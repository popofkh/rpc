package utils;

import center.Center;
import org.apache.zookeeper.KeeperException;

import javax.print.DocFlavor;
import java.util.Map;
import java.util.Set;

public class ZkUtil {

    /**
     * 初始化根节点和生产者消费者节点，使用持久节点
     */
    public static void initClientZnode(ZkClient client) throws KeeperException, InterruptedException {

        // 创建根节点
        String rootPath = ZkConst.ROOT_PATH;
        client.createZnode(rootPath, null);

        // 创建服务节点
        String servicePath = rootPath + ZkConst.SERVICE_PATH;
        client.createZnode(servicePath, null);

        // 获取配置文件中所有的服务名称，为每个服务创建节点
        Set<String> serviceNames = Center.getClientConfig().getServiceName();
        for (String serviceName : serviceNames) {
            client.createZnode(servicePath + "/" + serviceName, null);
            client.createZnode(servicePath + "/" + serviceName + ZkConst.CONSUMERS_PATH, null);
        }
    }

    /**
     * 初始化根节点和服务提供者节点，均为持久节点
     * 服务IP这里不处理，因为是可变的，需要注册为临时节点
     * @param client zookeeper客户端
     */
    public static void initServerZnode(ZkClient client) throws KeeperException, InterruptedException {

        // 创建/ROOT节点
        StringBuilder pathBuilder = new StringBuilder(ZkConst.ROOT_PATH);
        client.createZnode(pathBuilder.toString(), null);
        // 创建/ROOT/SERVICE节点
        pathBuilder.append(ZkConst.SERVICE_PATH);
        client.createZnode(pathBuilder.toString(), null);
        // 获取所有服务端xml中配置的服务
        Map<String, String> serviceNameToImpl = Center.getServerConfig().getServiceNameToImpl();
        // 在zk中注册服务
        for (Map.Entry<String, String> entry: serviceNameToImpl.entrySet()){

            StringBuilder serviceBuilder = new StringBuilder(pathBuilder.toString());
            serviceBuilder.append("/");
            serviceBuilder.append(entry.getKey());
            // 创建/ROOT/SERVICE/HelloService节点
            client.createZnode(serviceBuilder.toString(), null);
            serviceBuilder.append(ZkConst.PROVIDERS_PATH);
            // 创建/ROOT/SERVICE/HelloService/PROVIDERS节点
            client.createZnode(serviceBuilder.toString(), null);
        }
    }

    /**
     * 注册服务端提供的所有服务的ip+port
     * @param client
     */
    public static void registerService(ZkClient client) throws KeeperException, InterruptedException {
        Map<String, String> serviceNameToImpl = Center.getServerConfig().getServiceNameToImpl();
        String serverAddr = Center.getServerConfig().getHost() + ":" + Center.getServerConfig().getPort();
        for (Map.Entry<String, String> service : serviceNameToImpl.entrySet()) {
            // 注册每个服务发ip+port
            client.createTempZnode(ZkConst.ROOT_PATH + ZkConst.SERVICE_PATH +
                    "/" + service.getKey() + ZkConst.PROVIDERS_PATH + "/" + serverAddr, null);
        }
    }
}
