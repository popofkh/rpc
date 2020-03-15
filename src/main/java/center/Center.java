package center;

import client.RequestEntity;
import client.RequestInvocationHandler;

import java.lang.reflect.Proxy;
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

    public static Object getService(Class cls) {
        RequestInvocationHandler handler = new RequestInvocationHandler();
        Object service = Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, handler);
        return service;
    }
}
