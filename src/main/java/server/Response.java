package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 启动服务，创建netty-server，等待客户端请求
 */
public class Response {

    public static ServerConfig serverConfig = null;

    public Response(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void start() {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                // SO_BACKLOG对应的是tcp/ip协议listen函数中的backlog参数,初始化服务端可连接队列
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new LineBasedFrameDecoder(2048));
                        socketChannel.pipeline().addLast(new StringDecoder());
                        socketChannel.pipeline().addLast(new ResponseHandler());
                    }
                });
        try {
            // 服务端绑定端口，监听客户端连接
            ChannelFuture future = bootstrap.bind(serverConfig.getHost(), serverConfig.getPort()).sync();
            // 阻塞服务端线程，知道监听端口关闭
            future.channel().closeFuture().sync();
            System.out.println("server closed...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
