package center;

import client.ClientConfig;
import client.ChannelInfo;
import client.RequestEntity;
import client.RequestInvocationHandler;
import loadBalance.LoadBalance;
import loadBalance.ServiceInfo;
import org.apache.zookeeper.KeeperException;
import org.springframework.context.ApplicationContext;
import server.Response;
import server.ServerConfig;
import timer.Liveness;
import utils.ZkClient;
import utils.ZkUtil;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;

public class Center {

    /**
     * 客户端Spring容器上下文，用来获取ClientConfig配置
     */
    public static ApplicationContext clientContext;

    /**
     * 服务端Spring容器上下文，用来获取ServerConfig配置
     */
    public static ApplicationContext serverContext;

    /**
     * 全局调用次数
     */
    public static AtomicLong requestTimes = new AtomicLong(0);

    /**
     * 客户端发送请求/接收响应同步锁
     * key: requestId
     * value: RequestEntity
     */
    public static Map<String, RequestEntity> requestLock = new ConcurrentHashMap<>();

    /**
     * 连接锁，用于同步netty-client连接动作与获取sendingContext动作
     */
//    public static ReentrantLock connectLock = new ReentrantLock();

    /**
     * 全局读写锁，changeIp时为写操作，负载均衡chooseIp为读操作
     */
    public static Map<String, ReadWriteLock> serviceLockMap = new ConcurrentHashMap<>();

    /**
     * 服务名称到服务详细信息（）的映射
     */
    public static Map<String, ServiceInfo> serviceNameInfoMap = new ConcurrentHashMap<>();

    // 心跳包周期
    public static int internalSecond = 15;
    //IP地址 映射 对应的NIO Channel及其引用次数
    public static Map<String, ChannelInfo> IPChannelMap = new ConcurrentHashMap<>();
    // 健康连接
    public static Map<String, ChannelInfo> healthyChannel = new ConcurrentHashMap<>();
    // 亚健康连接
    public static Map<String, ChannelInfo> subhealthyChannel = new ConcurrentHashMap<>();

    /**
     * 负责心跳线程的管理
     */
    public static ScheduledExecutorService heartbeatThreadPool = Executors.newScheduledThreadPool(1);

    /**
     * 保存客户端配置的负载均衡策略
     */
    public static LoadBalance loadBalance;

    public static Timer timer = new Timer();

    static {
        heartbeatThreadPool.scheduleAtFixedRate(new Liveness(), 0, 15, TimeUnit.SECONDS);
    }

    /**
     * 客户端启动入口，获取远程代理对象
     * @param cls 目标服务的Class对象
     * @return 目标服务的代理对象
     */
    public static Object getService(Class cls) {
        RequestInvocationHandler handler = new RequestInvocationHandler();
        Object service = Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, handler);
        return service;
    }

    /**
     * 服务端启动入口，注册服务，等待客户端连接
     */
    public static void register() {
        // 创建服务端的zookeeper-client
        ZkClient client = new ZkClient(Center.getServerConfig().getZooKeeperAddr());

        try {
            // 初始化服务端对应的节点：/RPC/SERVICE/HelloService/PROVIDERS
            ZkUtil.initServerZnode(client);
            // 初始化服务addr节点：/RPC/SERVICE/HelloService/PROVIDERS/localhost:8888
            ZkUtil.registerService(client);

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 启动netty-server
        Response.start();
    }

    /**
     * 从客户端Spring容器上下文中获取客户端配置
     * @return
     */
    public static ClientConfig getClientConfig() {
        return clientContext.getBean(ClientConfig.class);
    }

    /**
     * 从服务端Spring容器上下文中获取服务端配置
     * @return
     */
    public static ServerConfig getServerConfig() {
        return serverContext.getBean(ServerConfig.class);
    }

    /**
     * 获取定时器
     * @return
     */
    public static Timer getTimer() {
        return timer;
    }
}
