package server;

import client.RequestEntity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvokeService {

    public static Object invoke(RequestEntity requestEntity) {
        // 获取请求服务的实现类名、请求方法名、请求参数、参数类型
        String serviceName = requestEntity.getServiceName();
        String serviceImplName = Response.serverConfig.getServiceNameToImpl().get(serviceName);
        System.out.println("serviceImplName: " + serviceImplName);
        String methodName = requestEntity.getMethodName();
        Object[] parameters = requestEntity.getParameters();
        Class[] parameterTypes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].getClass();
        }

        Object result = null;
        try {
            Class<?> targetImpl = Class.forName(serviceImplName);
            // 执行请求方法
            Method targetMethod = targetImpl.getDeclaredMethod(methodName, parameterTypes);
            Object targetInstance = targetImpl.newInstance();
            result = targetMethod.invoke(targetInstance, parameters);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        System.out.println(result);

        return result;
    }
}
