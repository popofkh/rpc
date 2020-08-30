package server;

import center.Center;
import client.RequestEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import utils.JsonUtil;

public class ResponseHandler extends ChannelInboundHandlerAdapter {

    private ByteBuf buildHeartbeat() {
        ResponseEntity heartbeat = new ResponseEntity();
        heartbeat.setLiveness(true);
        heartbeat.setAddr(Center.getServerConfig().getHost() + ":" + Center.getServerConfig().getPort());
        String responseJson = null;
        try {
            responseJson = JsonUtil.responseEncode(heartbeat);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ByteBuf responseBuf = Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8);
        return responseBuf;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String requestJson = (String) msg;
        System.out.println(msg);
        RequestEntity requestEntity = JsonUtil.requestDecode(requestJson);
        // 检查是否是心跳包
        if(requestEntity.getLiveness()) {
            ByteBuf byteBuf = buildHeartbeat();
            ctx.writeAndFlush(requestEntity);
            return;
        }

        // TODO 方法的执行不应该在netty线程中完成，应该交给server-stub中的线程池执行，并通过回调函数，发送response
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
