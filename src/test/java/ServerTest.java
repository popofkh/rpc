import center.Center;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerTest {

    /**
     * 1台server，提供多个不同的service
     */
    @Test
    public void singleServerTest() {
        ApplicationContext context = new ClassPathXmlApplicationContext("ServerContext.xml");
        Center.register();
    }

    /**
     * 启动多个服务提供者
     */
    @Test
    public void mutiServerTest() {

    }
}
