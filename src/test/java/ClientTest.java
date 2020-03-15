import center.Center;
import org.junit.Test;

public class ClientTest {

    @Test
    public void test() throws Exception{
        HelloService helloService = (HelloService) Center.getService(HelloService.class);
        String response = helloService.sayHello("fangkanghua");
        System.out.println(response);
    }
}
