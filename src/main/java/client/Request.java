package client;

import center.Center;
import com.fasterxml.jackson.core.JsonProcessingException;
import exception.ServiceUnavailableException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;
import timer.RequestTimer;
import utils.JsonUtil;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * 创建客户端连接，获取netty-client的sendingcontext
 */
public class Request {

    /**
     * 用于发送消息的ChannelHandlerContext，从RequestHandler中获取
     */
    public static ChannelHandlerContext sendingContext = null;
    /**
     * 用于发送请求的NettyClient实例
     */
    private static Request instance = null;

    private Request() {}

    /**
     * 使用Double check保证不重复对同一个IP创建连接
     * @param addr
     * @return
     */
    private Channel getServiceChannel(String addr) {
        if(Center.IPChannelMap.get(addr).getChannel() == null) {
            synchronized (Center.IPChannelMap.get(addr)) {
                if (Center.IPChannelMap.get(addr).getChannel() == null) {
                    // netty线程租
                    EventLoopGroup group = new NioEventLoopGroup();
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(group)
                            .channel(NioSocketChannel.class)
                            // 禁用nagle算法
                            .option(ChannelOption.TCP_NODELAY, true)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel socketChannel) throws Exception {
                                    socketChannel.pipeline().addLast(new LineBasedFrameDecoder(2048));
                                    socketChannel.pipeline().addLast(new StringDecoder());
                                    socketChannel.pipeline().addLast(new RequestHandler());
                                }
                            });
                    try {
                        ChannelFuture future = bootstrap.connect(addr.split(":")[0], Integer.parseInt(addr.split(":")[1])).sync();
                        future.addListener(new ChannelFutureListener() {
                            // TODO 这个函数里的channelFuture是什么
                            @Override
                            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                System.out.println("connect server " + addr + " completed...");
//                            Center.IPChannelMap.get(addr).setChannel(channelFuture.channel());
//                            Center.IPChannelMap.get(addr).setGroup(group);
                            }
                        });
                        // 缓存channel
                        Center.IPChannelMap.get(addr).setChannel(future.channel());
                        Center.IPChannelMap.get(addr).setGroup(group);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return Center.IPChannelMap.get(addr).getChannel();
    }

    /**
     * 使用DCL单例模式，获取netty-client实例
     * @return
     */
    public static Request getInstance() {
        if(instance == null) {
            synchronized (Request.class) {
                if(instance == null) {
                    instance = new Request();
                }
            }
        }
        return instance;
    }

    public void send(RequestEntity requestEntity) {
        // 根据负载均衡策略确定服务端地址
        String serviceName = requestEntity.getServiceName();
        // TODO 在loadBalance中配置调用方地址后，才能调用负载均衡策略
        String serviceAddr = Center.loadBalance.chooseAddr(serviceName);

        try {
            String requestJson = JsonUtil.requestEncode(requestEntity);
            ByteBuf requsetBuf = Unpooled.copiedBuffer(requestJson, CharsetUtil.UTF_8);
            System.out.println("sending request: " + requestJson);

            // 超时重传
            int requestCnt = 0;
            while (requestCnt < 3) {
                // TODO 等待连接建立，应该在Center中维护一个Map，应该是等待特定IP对应的channel连接建立
                Channel channel = getServiceChannel(serviceAddr);
                channel.writeAndFlush(requsetBuf);
                // 使用java原生Timer实现定时器功能，定时器只执行一次
                Center.getTimer().schedule(new RequestTimer(requestEntity), Center.getClientConfig().getTimeout());
                // 同步代码块，等待请求返回的result写入request中,由于调用wait()后如果没有相应会导致死锁，所以必须有线程主动调用notiify()才能避免死锁
                synchronized (requestEntity) {
                    requestEntity.wait();
                }
                if (requestEntity.getResult() == null) {
                    requestCnt++;
                    channel = getServiceChannel(serviceAddr);
                    channel.writeAndFlush(requsetBuf);
                }
                else break;      // 如果请求成功直接跳出循环，返回结果
            }
            if(requestCnt == 3) throw new ServiceUnavailableException(serviceName + " unavailable...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
