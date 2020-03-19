package loadBalance;

public class RandomBalance implements LoadBalance {
    @Override
    public String chooseAddr(String serviceName) {
        return "localhost:8888";
    }
}
