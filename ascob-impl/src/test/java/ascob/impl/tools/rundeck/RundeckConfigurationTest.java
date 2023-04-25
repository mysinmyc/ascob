package ascob.impl.tools.rundeck;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ActiveProfiles({"test","testrundeck"})
public class RundeckConfigurationTest {

    @Configuration
    @ComponentScan
    static class SpringConfigurationClass {

    }

    @Test
    public void testConfiguration(@Autowired RundeckConfiguration configuration) {

            assertEquals(2, configuration.getInstances().size());
            assertEquals("http://localhost:4440", configuration.getInstances().get("default").getUrl());
            assertEquals("token", configuration.getInstances().get("default").getAuthToken());
            assertEquals("http://other:4440", configuration.getInstances().get("other").getUrl());
            assertEquals("other", configuration.getInstances().get("other").getAuthToken());
    }
}
