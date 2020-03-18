package utils;

import client.RequestEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import server.ResponseEntity;

import java.io.IOException;

public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 编码请求体
     * @param requestEntity 请求体对象
     * @return json字符串
     * @throws JsonProcessingException
     */
    public static String requestEncode(RequestEntity requestEntity) throws JsonProcessingException {
        return objectMapper.writeValueAsString(requestEntity) + System.getProperty("line.separator");
    }

    /**
     * 解码请求体
     * @param requestJson json字符串
     * @return 解析后的请求体
     * @throws IOException
     */
    public static RequestEntity requestDecode(String requestJson) throws IOException {
        return objectMapper.readValue(requestJson, RequestEntity.class);
    }

    /**
     * 编码响应体
     * @param response 响应体
     * @return 编码后的json对象
     * @throws JsonProcessingException
     */
    public static String responseEncode(ResponseEntity response) throws JsonProcessingException {
        return objectMapper.writeValueAsString(response) + System.getProperty("line.separator");
    }

    /**
     * 解码响应体
     * @param json json字符串
     * @return 解码后的响应体
     * @throws IOException
     */
    public static Object responseDecode(String responseJson) throws IOException {
        return objectMapper.readValue(responseJson, ResponseEntity.class);
    }
}
