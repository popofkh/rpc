package center;

import client.ClientConfig;
import client.RequestEntity;
import client.RequestInvocationHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import server.Response;
import server.ServerConfig;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

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
    public static ReentrantLock connectLock = new ReentrantLock();

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
}
