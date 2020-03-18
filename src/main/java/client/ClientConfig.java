package client;

import center.Center;
import loadBalance.LoadBalance;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import utils.ZkClient;
import utils.ZkUtil;

import java.util.Set;

public class ClientConfig implements ApplicationContextAware {

    /**
     * ZooKeeper服务地址
     */
    private String zooKeeperAddr;

    /**
     * 超时时间
     */
    private long timeout;

    /**
     * 客户端需要调用的服务
     */
    private Set<String> serviceName;

    /**
     * 选用的负载均衡策略
     */
    private LoadBalance loadBalance;

    public Set<String> getServiceName() {
        return serviceName;
    }

    public void setServiceName(Set<String> serviceName) {
        this.serviceName = serviceName;
    }

    public LoadBalance getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    public String getZooKeeperAddr() {
        return zooKeeperAddr;
    }

    public void setZooKeeperAddr(String zooKeeperAddr) {
        this.zooKeeperAddr = zooKeeperAddr;
    }

    /**
     * 默认构造函数，Spring会用到，不写无法初始化Bean
     */
    public ClientConfig() {
    }

    public ClientConfig(String zooKeeperAddr, long timeout) {
        this.zooKeeperAddr = zooKeeperAddr;
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * 缓存客户端Spring容器上下文，用于程序运行时获取IoC中的配置Bean
     * 同时初始化zk节点
     * @param applicationContext 当前Spring容器的上下文
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Center.clientContext = applicationContext;

//        try {
//            ZkClient client = new ZkClient(Center.getClientConfig().getZooKeeperAddr());
//            ZkUtil.initZnode(client);
//
//
//        } catch (KeeperException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
