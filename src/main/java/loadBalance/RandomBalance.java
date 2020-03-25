package loadBalance;

import center.Center;
import exception.ProviderNotFoundException;
import exception.ServiceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomBalance implements LoadBalance {
    @Override
    public String chooseAddr(String serviceName) {
        if (Center.serviceLockMap.get(serviceName) == null) {
            throw new ServiceNotFoundException(serviceName + " not found...");
        }
        // 对目标服务节点加读锁
        Center.serviceLockMap.get(serviceName).readLock().lock();
        // 获取目标服务的IP
        Set<String> serviceIPSet = Center.serviceNameInfoMap.get(serviceName).getServiceIPSet();

        if (serviceIPSet.size() == 0) {
            throw new ProviderNotFoundException("Provider for " + serviceName + " not found...");
        }

        // 随机数 [0, ipset.size()]
        List<String> ipList = new ArrayList<>(serviceIPSet);
        return ipList.get(new Random().nextInt(serviceIPSet.size()));
    }
}
