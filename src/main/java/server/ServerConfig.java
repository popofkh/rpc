package server;

import center.Center;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

public class ServerConfig implements ApplicationContextAware {

    /**
     * 服务端ip
     */
    private String host;

    /**
     * 服务端port
     */
    private int port;

    /**
     * 服务端接口名到实现类名的映射，用于反射调用
     */
    private Map<String, String> serviceNameToImpl = new HashMap<>();

    /**
     * 默认构造函数，Spring初始化Bean用到
     */
    public ServerConfig() {
    }

    public ServerConfig(String host, int port, Map<String, String> serviceNameToImpl) {
        this.host = host;
        this.port = port;
        this.serviceNameToImpl = serviceNameToImpl;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String> getServiceNameToImpl() {
        return serviceNameToImpl;
    }

    public void setServiceNameToImpl(Map<String, String> serviceNameToImpl) {
        this.serviceNameToImpl = serviceNameToImpl;
    }

    /**
     * 缓存Server端Spring容器的上下文，以供程序运行时获取Server配置
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Center.serverContext = applicationContext;
    }
}
