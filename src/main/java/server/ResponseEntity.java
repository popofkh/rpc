package server;

/**
 * 处理结果实体类
 */
public class ResponseEntity {

    // 全局唯一的请求id，用于对应response和request
    private String requestId;
    // 处理结果
    private Object result;
    // 心跳包标记
    private boolean liveness;
    // 服务地址
    private String addr;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public boolean getLiveness() {
        return liveness;
    }

    public void setLiveness(boolean liveness) {
        this.liveness = liveness;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
