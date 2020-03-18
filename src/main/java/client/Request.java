package client;

import center.Center;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import utils.JsonUtil;

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

    private Request() {

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
            ChannelFuture future = bootstrap.connect("", 8888).sync();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    System.out.println("connect completed...");
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
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
        try {
            // 等待连接建立
            synchronized (Center.connectLock) {
                while (Request.sendingContext == null) {
                    Center.connectLock.wait();
                }
            }
            String requestJson = JsonUtil.requestEncode(requestEntity);
            ByteBuf requsetBuf = Unpooled.copiedBuffer(requestJson, CharsetUtil.UTF_8);
            sendingContext.writeAndFlush(requsetBuf);
            System.out.println("sending request: " + requestJson);

            // 等待请求返回的result写入request中，释放对象锁
            synchronized (requestEntity) {
                requestEntity.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
