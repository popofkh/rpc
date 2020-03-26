import center.Center;
import exception.ServiceNotFoundException;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
        String helloResponse = helloService.sayHello("fangkanghua");
        System.out.println(helloResponse);

        HiService hiService = (HiService) Center.getService(HiService.class);
        String hiResponse = hiService.sayHi("fangkanghua");
        System.out.println(hiResponse);

        try {
            NoneService noneService = (NoneService) Center.getService(NoneService.class);
            String noneResponse = noneService.sayNone("fangkanghua");
            System.out.println(noneResponse);
        } catch (ServiceNotFoundException e) {
            System.out.println("Service not found...");
        }
    }
}
