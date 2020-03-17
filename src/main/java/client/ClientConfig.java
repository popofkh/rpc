package client;

import center.Center;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ClientConfig implements ApplicationContextAware {

    /**
     * 目标服务地址
     */
    private String host;
    /**
     * 目标服务端口
     */
    private int port;
    /**
     * 超时时间
     */
    private long timeout;

    /**
     * 默认构造函数，Spring会用到，不写无法初始化Bean
     */
    public ClientConfig() {
    }

    public ClientConfig(String host, int port, long timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
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

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * 缓存客户端Spring容器上下文，用于程序运行时获取IoC中的配置Bean
     * @param applicationContext 当前Spring容器的上下文
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Center.clientContext = applicationContext;
    }
}
