package timer;

import center.Center;
import client.ChannelInfo;
import client.RequestEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import utils.JsonUtil;

import java.util.Map;

public class Liveness implements Runnable {
    // 对缓存的channel定时发送心跳，维护三个channel map(健康、亚健康、非健康)
    @Override
    public void run() {
        // 对于首次建立的连接和healthy channel，每隔15s一次心跳包，如果出现连续三次超时(或一定时间内业务调用成功率在70%以下)，移入subhealthyChannelMap
        for(String addr : Center.healthyChannel.keySet()) {
            ChannelInfo channelInfo = Center.healthyChannel.get(addr);
            // 连续三次不正常，channel降级
            if(checkTimeout(channelInfo,3)) {
                Center.healthyChannel.remove(addr);
                Center.subhealthyChannel.put(addr, channelInfo);
                continue;
            }
            // 发送心跳
            ByteBuf heartBeat = buildHeatBeat();
            channelInfo.getChannel().writeAndFlush(heartBeat);
        }

        // 对于subhealthy channel(因网络抖动或负载过高造成的不稳定连接)，连续三次心跳稳定后可以重新成为healthy channel
        // 如果超过2h处于不健康的状态，将断开连接。问题在于，什么断开连接后，什么时候能恢复呢？（注意：不能采取删除注册节点的做法，因为可能会影响别的刻划断通信）
        // 每隔3h，对注册中心的服务addr进行扫描，看看原来因为网络抖动等原因释放的目标机器间能否重新建立healthy的连接
        for(String addr : Center.subhealthyChannel.keySet()) {
            ChannelInfo channelInfo = Center.subhealthyChannel.get(addr);
            // 连续三次正常，channel升级
            if(channelInfo.getCount() > 3) {
                Center.healthyChannel.put(addr, channelInfo);
                Center.subhealthyChannel.remove(addr);
                continue;
            }
            ByteBuf heartBeat = buildHeatBeat();
            channelInfo.getChannel().writeAndFlush(heartBeat);
        }
    }

    /**
     * 判断channel是否过期（超过三次心跳未响应）
     * @param channelInfo channel信息
     * @param n 表示心跳周期的n倍
     * @return
     */
    private boolean checkTimeout(ChannelInfo channelInfo, int n) {
        if(System.currentTimeMillis() - channelInfo.getLastUpdateTime() > n * Center.internalSecond) return true;
        else return false;
    }

    /**
     * 创建心跳包
     * @return
     */
    private ByteBuf buildHeatBeat() {
        RequestEntity heartbeat = new RequestEntity();
        heartbeat.setLiveness(true);
        // TODO 需要使用requestID吗
        String requestJson = null;
        try {
            requestJson = JsonUtil.requestEncode(heartbeat);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ByteBuf heartbeatBuf = Unpooled.copiedBuffer(requestJson, CharsetUtil.UTF_8);
        return heartbeatBuf;
    }
}
