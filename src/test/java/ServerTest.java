import center.Center;
import com.sun.security.ntlm.NTLMException;
import com.sun.security.ntlm.Server;
import org.junit.Before;
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
        new ServerTest().server();
    }

    /**
     * 启动多个服务提供者
     */
    @Test
    public void mutiServerTest() {
        new ServerTest().server();
        new ServerTest().server1();
    }

    private void server() {
        ApplicationContext context = new ClassPathXmlApplicationContext("ServerContext.xml");
        Center.register();
    }
    private void server1() {
        ApplicationContext context = new ClassPathXmlApplicationContext("ServerContext1.xml");
        Center.register();
    }

}
