import center.Center;
import org.junit.Test;

public class ClientTest {

    /**
     * 单个调用者
     * @throws Exception
     */
    @Test
    public void singleClientTest() throws Exception {
        HelloService helloService = (HelloService) Center.getService(HelloService.class);
        String response = helloService.sayHello("fangkanghua");
        System.out.println(response);
    }
}
