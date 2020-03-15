package server;

/**
 * 处理结果实体类
 */
public class ResponseEntity {

    // 全局唯一的请求id，用于对应response和request
    private String requestId;
    // 处理结果
    private Object result;

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
}
