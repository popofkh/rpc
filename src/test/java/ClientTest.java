import center.Center;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientTest {

    /**
     * 单个调用者
     * @throws Exception
     */
    @Test
    public void singleClientTest() throws Exception {
        // 启动Spring容器，加载Client配置
        ApplicationContext context = new ClassPathXmlApplicationContext("ClientContext.xml");

        HelloService helloService = (HelloService) Center.getService(HelloService.class);
        String response = helloService.sayHello("fangkanghua");
        System.out.println(response);
    }
}
