import center.Center;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerTest {

    /**
     * 单个服务提供者
     */
    @Test
    public void singleServerTest() {
        ApplicationContext context = new ClassPathXmlApplicationContext("ServerContext.xml");
        Center.register();
    }
}
