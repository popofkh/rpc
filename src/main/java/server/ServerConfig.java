package server;

import java.util.HashMap;
import java.util.Map;

public class ServerConfig {

    /**
     * 服务端ip
     */
    private String host = "localhost";

    /**
     * 服务端port
     */
    private int port = 8888;

    /**
     * 服务端接口名到实现类名的映射，用于反射调用
     */
    private Map<String, String> serviceNameToImpl = new HashMap<>();

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
}
