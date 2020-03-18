package utils;

import center.Center;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;


public class ZkClient {

    private ZooKeeper zooKeeper;

    /**
     * 构造函数，初始化zooKeeper客户端
     */
    public ZkClient(String host) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            // 创建zk客户端，并注册默认的Watcher事件通知处理器
            this.zooKeeper = new ZooKeeper(host, ZkConst.SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
                        countDownLatch.countDown();
                    }
                }
            });
            // 阻塞，等待zk客户端完成创建
            countDownLatch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建持久节点，已存在则不创建
     * @param path 节点路径
     * @param data 节点数据
     */
    public void createZnode(String path, String data) throws KeeperException, InterruptedException {
        Stat stat = znodeExists(path);
        if (stat != null) {
            return;
        }
        byte[] bytes = (data == null) ? null : data.getBytes();
        zooKeeper.create(path, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    /**
     * 创建临时节点
     * @param path 节点路径
     * @param data 节点数据
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void createTempZnode(String path, String data) throws KeeperException, InterruptedException {
        byte[] bytes = (data == null) ? null : data.getBytes();
        zooKeeper.create(path, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

    /**
     * 创建临时顺序节点
     * @param path 节点路径
     * @param data 节点数据
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void createTempSeqZnode(String path,String data) throws KeeperException, InterruptedException {
        byte[] bytes = (data == null) ? null : data.getBytes();
        zooKeeper.create(path, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    /**
     * 判断节点是否存在
     * @param path
     * @return
     */
    public Stat znodeExists(String path) throws KeeperException, InterruptedException {
        // 这里watch需要用true吗？
        return zooKeeper.exists(path, false);
    }
}
