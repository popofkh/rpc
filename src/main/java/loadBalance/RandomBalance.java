package loadBalance;

import center.Center;
import exception.ProviderNotFoundException;
import exception.ServiceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RandomBalance implements LoadBalance {
    @Override
    public String chooseAddr(String serviceName) {
        if (Center.serviceLockMap.get(serviceName) == null) {
            throw new ServiceNotFoundException(serviceName + " not found...");
        }
        // 存放目标服务的IP
        Set<String> serviceIPSet;
        // 对目标服务节点加读锁
        Lock lock = Center.serviceLockMap.get(serviceName).readLock();
        try {
            lock.lock();
            serviceIPSet = Center.serviceNameInfoMap.get(serviceName).getServiceIPSet();
        } finally {
            lock.unlock();
        }
        if (serviceIPSet.size() == 0) {
            throw new ProviderNotFoundException("Provider for " + serviceName + " not found...");
        }

        // 随机数 [0, ipset.size()]
        List<String> ipList = new ArrayList<>(serviceIPSet);
        return ipList.get(new Random().nextInt(serviceIPSet.size()));
    }

    @Override
    public void changeAddr(String serviceName, List<String> newAddrSet) {
        Center.serviceNameInfoMap.get(serviceName).setServiceIPSet(newAddrSet);
    }
}
