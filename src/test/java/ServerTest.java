import center.Center;
import org.junit.Test;

public class ServerTest {

    /**
     * 单个服务提供者
     */
    @Test
    public void singleServerTest() {
        Center.register();
    }
}
