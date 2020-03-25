package loadBalance;

import center.Center;
import client.IPChannelInfo;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import utils.ZkClient;

import java.util.List;

/**
 * 监听服务ip变化的Watcher
 */
public class IpWatcher implements Watcher {

    private ZkClient client;

    public IpWatcher(ZkClient client) {
        this.client = client;
    }

    /**
     * 当IP变化时被调用
     * @param watchedEvent
     */
    @Override
    public void process(WatchedEvent watchedEvent) {
        // 获取watcher所在节点路径
        String path = watchedEvent.getPath();
        // 获取serviceName
        String serviceName = path.split("/")[3];
        // 写IP操作
        Center.serviceLockMap.get(serviceName).writeLock().lock();
        System.out.println("Providers changed... Lock write lock...");

        try {
            // 缓存目标服务的所有ip
            List<String> ips = client.getZnodeChildren(path, this);
            System.out.println(ips.size());
            for (String ip : ips) {
                Center.IPChannelMap.putIfAbsent(ip, new IPChannelInfo());
            }
            // 更新服务ip
            Center.serviceNameInfoMap.get(serviceName).setServiceIPSet(ips);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

        // 释放写锁
        Center.serviceLockMap.get(serviceName).writeLock().unlock();
        System.out.println("Providers changing committed, unlock write lock...");
    }
}
