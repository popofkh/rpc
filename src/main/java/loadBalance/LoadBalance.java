package loadBalance;

/**
 * 负载均衡策略接口
 */
public interface LoadBalance {
    String chooseAddr(String serviceName);
}
