package loadBalance;

import center.Center;

import java.util.*;

public class ConsistentHashBalance implements LoadBalance {
    /**
     * 服务->(虚拟节点->真实节点)的映射关系
     * 外层key为服务名
     * 内层key为虚拟节点对应的32位整数，value为真实服务器的地址
     */
    private Map<String, SortedMap<Integer, String>> serviceVirtualRealMap= new HashMap<>();

    private int virturalNodeNum;

    private String consumerAddr;

    public ConsistentHashBalance() {

    }

    public int getVirturalNodeNum() {
        return virturalNodeNum;
    }

    public void setVirtualNodeNum(int virturalNodeNum) {
        this.virturalNodeNum = virturalNodeNum;
    }

    public String getConsumerAddr() {
        return consumerAddr;
    }

    public void setConsumerAddr(String consumerAddr) {
        this.consumerAddr = consumerAddr;
    }

    /**
     * 初始化，加入真实结点
     * @throws ProvidersNoFoundException
     */
    private void init() {
        Set<String> serviceNames = Center.getClientConfig().getServiceNames();
        for (String serviceName : serviceNames) {
            // 构造虚拟节点到真实节点的映射
            SortedMap<Integer, String> virtualRealMap =  new TreeMap<>();
            Set<String> serviceAddrSet = Center.serviceNameInfoMap.get(serviceName).getServiceIPSet();
            for (String serviceAddr : serviceAddrSet) {
                addRealNode(serviceAddr, virtualRealMap);
            }
            serviceVirtualRealMap.put(serviceName, virtualRealMap);
        }
    }

    /**
     * 为单个真实节点地址添加虚拟节点映射
     * @param serviceAddr 真实节点地址
     * @param virtualRealMap 单个服务的哈希环
     */
    private void addRealNode(String serviceAddr, SortedMap<Integer, String> virtualRealMap) {
        if (!virtualRealMap.containsKey(getHash(serviceAddr))) {
            for(int i = 0; i < virturalNodeNum; i++) {
                String virtualAddr = serviceAddr + "-" + i;
                virtualRealMap.put(getHash(virtualAddr), serviceAddr);
            }
        }
    }

    /**
     * 删除离线的真实节点
     * @param serviceName 服务名称
     * @param oldAddr 下线的IP
     */
    private void removeRealNode(String serviceName, String oldAddr) {
        SortedMap<Integer, String> virtualRealMap = serviceVirtualRealMap.get(serviceName);
        for (int i = 0; i < virturalNodeNum; i++) {
            String virtualAddr = oldAddr + "-" + i;
            serviceVirtualRealMap.get(serviceName).remove(getHash(virtualAddr));
        }
    }

    /**
     * 服务调用方地址经过哈希后，获取真实服务器地址
     * @param key 服务名称
     * @return
     */
    public String getRealAddr(String serviceName) {
        // 获取对应服务的哈希环（虚拟节点->真实节点）
        SortedMap<Integer, String> virtualRealMap = serviceVirtualRealMap.get(serviceName);
        // 沿环的顺时针找到一个虚拟节点，tailMap的作用是截取键值大于给定值的尾部Map
        // 如果不存在比给定键值更大的键，对应到最小的键
        SortedMap<Integer, String> tail = virtualRealMap.tailMap(getHash(consumerAddr));
        int virtualAddr = (tail.size() == 0) ? virtualRealMap.firstKey() : tail.firstKey();
        // 根据虚拟节点返回真实服务器地址
        return virtualRealMap.get(virtualAddr);
    }

    public int getHash(String key) {
        return FNVHash(key);
    }

    public int FNVHash(String key) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < key.length(); i++) {
            hash = (hash ^ key.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) hash = Math.abs(hash);
        return hash;
    }

    @Override
    public String chooseAddr(String serviceName) {
        if(serviceVirtualRealMap.size() == 0) {
            init();
        }
        return getRealAddr(serviceName);
    }

    @Override
    public void changeAddr(String serviceName, List<String> newAddrSet) {
        Set<String> oldAddrSet = Center.serviceNameInfoMap.get(serviceName).getServiceIPSet();
        // 找出离线节点，从哈希环中删除
        oldAddrSet.removeAll(newAddrSet);
        for (String oldAddr : oldAddrSet) {
            removeRealNode(serviceName, oldAddr);
        }
        // 更新（service->addr）的映射
        Center.serviceNameInfoMap.get(serviceName).setServiceIPSet(newAddrSet);
    }
}
