package loadBalance;

import java.util.List;
import java.util.Map;

/**
 * 负载均衡策略接口
 */
public interface LoadBalance {
    String chooseAddr(String serviceName);

    void changeAddr(String serviceName, List<String> newAddrSet);
}
