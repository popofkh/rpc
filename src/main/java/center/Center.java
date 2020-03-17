package center;

import client.RequestEntity;
import client.RequestInvocationHandler;
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
        HashMap<String, String> serviceNameToImpl = new HashMap<>();
        serviceNameToImpl.put("HelloService", "HelloServiceImpl");
        ServerConfig serverConfig = new ServerConfig("localhost", 8888, serviceNameToImpl);
        new Response(serverConfig).start();
    }
}
