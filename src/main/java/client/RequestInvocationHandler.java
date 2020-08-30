package client;

import center.Center;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RequestInvocationHandler implements InvocationHandler {
    /**
     * 远程服务的代理类执行方法，负责传递请求，接收响应，传给Client
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构造请求实体
        // TODO 支持异步调用的情况，还需封装一个callback对象
        RequestEntity requestEntity = new RequestEntity();
        requestEntity.setRequestId(buildRequestId());
        requestEntity.setServiceName(method.getDeclaringClass().getName());
        requestEntity.setMethodName(method.getName());
        requestEntity.setParameters(args);

        // 将调用信息存入专门保存调用信息的全局ConcurrentHashMap中，因为存在多线程同时发起远程调用的情况，必须使用线程安全的Map
        Center.requestLock.put(requestEntity.getRequestId(), requestEntity);
        // send方法中使用了wait方法，请求发出后会阻塞，等待请求结果result写入request中
        Request.getInstance().send(requestEntity);
        // 调用结束，移除本次调用信息
        Center.requestLock.remove(requestEntity.getRequestId());
        // 返回调用结果
        return requestEntity.getResult();
    }


    /**
     * 生成全局唯一的requestId：调用次数+时间戳
     * @param methodName
     * @return
     */
    private String buildRequestId() {
        StringBuilder builder = new StringBuilder();
        builder.append(Center.requestTimes.getAndIncrement());
        builder.append(System.currentTimeMillis());
        return builder.toString();
    }
}
