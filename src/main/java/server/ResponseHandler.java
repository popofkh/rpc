package server;

import client.RequestEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import utils.JsonUtil;

public class ResponseHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String requestJson = (String) msg;
        System.out.println(msg);
        RequestEntity requestEntity = JsonUtil.requestDecode(requestJson);
        // 检查是否是心跳包
        if(requestEntity.getLiveness()) {

        }

        // 执行目标方法，构造响应体
        Object result = InvokeService.invoke(requestEntity);

        ResponseEntity responseEntity = new ResponseEntity();
        responseEntity.setRequestId(requestEntity.getRequestId());
        responseEntity.setResult(result);

        // 响应体编码，发送给调用方
        String responseJson = JsonUtil.responseEncode(responseEntity);
        ByteBuf responseBuf = Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8);
        System.out.println("responseJson: " + responseJson);
        ctx.writeAndFlush(responseBuf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 远程client关闭时，关闭次channel
        ChannelFuture future = ctx.close();
        future.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                System.out.println("");
            }
        });
    }
}
