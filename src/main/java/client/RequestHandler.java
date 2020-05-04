package client;

import center.Center;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.ResponseEntity;
import utils.JsonUtil;

public class RequestHandler extends ChannelInboundHandlerAdapter {

    /**
     * Channel建立连接时，初始化ChannelHandlerContext，并使用该context执行send操作
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        Request.sendingContext = ctx;
//        // 唤醒等待连接建立的线程，使其可以进行send发送消息操作
//        synchronized (Center.connectLock) {
//            Center.connectLock.notifyAll();
//        }
    }

    /**
     * 接收服务端发回的result
     * @param ctx ChannelHandler上下文
     * @param msg 服务端请求处理的结果
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String responseJson = (String) msg;
        System.out.println("receiveing..." + responseJson);
        ResponseEntity responseEntity = (ResponseEntity) JsonUtil.responseDecode(responseJson);
        // 处理心跳包
        if(responseEntity.getLiveness()) {
            dealHeartbeat(responseEntity);
        }
        // 将处理结果放入requestLock对应的request中，并唤醒等待结果的client
        synchronized (Center.requestLock.get(responseEntity.getRequestId())) {
            RequestEntity requestEntity = Center.requestLock.get(responseEntity.getRequestId());
            requestEntity.setResult(responseEntity.getResult());
            requestEntity.notifyAll();
        }
    }

    /**
     * 处理心跳响应包,更新最近心跳时间和连续响应心跳次数
     * @param responseEntity
     */
    private void dealHeartbeat(ResponseEntity responseEntity) {
        String addr = responseEntity.getAddr();
        if(Center.healthyChannel.containsKey(addr)) {
            if(System.currentTimeMillis() - Center.healthyChannel.get(addr).getLastUpdateTime() < 3 + Center.internalSecond) {
                Center.healthyChannel.get(addr).increaseCount();
            }
            else {
                Center.subhealthyChannel.get(addr).clearCount();
            }
            Center.healthyChannel.get(addr).setLastUpdateTime(System.currentTimeMillis());
            return;
        }
        if(Center.subhealthyChannel.containsKey(addr)) {
            if(System.currentTimeMillis() - Center.healthyChannel.get(addr).getLastUpdateTime() < 3 + Center.internalSecond) {
                Center.subhealthyChannel.get(addr).increaseCount();
            }
            else {
                Center.subhealthyChannel.get(addr).clearCount();
            }
            Center.subhealthyChannel.get(addr).setLastUpdateTime(System.currentTimeMillis());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
