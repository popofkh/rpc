package client;

import center.Center;
import loadBalance.LoadBalance;
import loadBalance.ServiceInfo;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import utils.ZkClient;
import utils.ZkUtil;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private Set<String> serviceNames;

    /**
     * 选用的负载均衡策略
     */
    private LoadBalance loadBalance;

    public Set<String> getServiceNames() {
        return serviceNames;
    }

    public void setServiceNames(Set<String> serviceNames) {
        this.serviceNames = serviceNames;
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
        // 缓存负载均衡策略
        Center.loadBalance = Center.getClientConfig().getLoadBalance();

        // 容器启动后，缓存服务端注册的服务及对应的ip+port
        try {
            ZkClient client = new ZkClient(Center.getClientConfig().getZooKeeperAddr());
            // 获取客户端配置的服务名称，并获取对应的提供者IP
            Set<String> serviceNames = Center.getClientConfig().getServiceNames();
            //初始化所有可用IP 初始化读写锁
            for (String serviceName : serviceNames){
                List<String> ips = ZkUtil.getServiceIps(client, serviceName);
                for (String ip : ips) {
                    Center.IPChannelMap.putIfAbsent(ip, new IPChannelInfo());
                }
                ServiceInfo serviceInfo=new ServiceInfo();
                serviceInfo.setServiceIPSet(ips);
                ReadWriteLock readWriteLock=new ReentrantReadWriteLock();
                Center.serviceLockMap.putIfAbsent(serviceName, readWriteLock);
                Center.serviceNameInfoMap.putIfAbsent(serviceName, serviceInfo);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
