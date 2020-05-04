package client;

/**
 * 调用端发送的请求实体类，保存了请求返回的结果
 */
public class RequestEntity {
    private String requestId;
    private String serviceName;
    private String methodName;
    private Object[] parameters;
    private Object result;
    private boolean liveness;
    private String addr;    // 心跳包对应的channel远端地址

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestID) {
        this.requestId = requestID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public boolean getLiveness() {
        return liveness;
    }

    public void setLiveness(boolean liveness) {
        this.liveness = liveness;
    }
}
