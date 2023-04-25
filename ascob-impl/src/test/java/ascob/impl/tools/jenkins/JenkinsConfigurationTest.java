package ascob.impl.tools.jenkins;


import ascob.impl.tools.jenkins.JenkinsConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ActiveProfiles({"test","testjenkins"})
public class JenkinsConfigurationTest {

    @Configuration
    @ComponentScan
    static class SpringConfigurationClass {

    }

    @Test
    public void testConfiguration(@Autowired JenkinsConfiguration configuration) {

        assertEquals(2, configuration.getInstances().size());

        JenkinsInstanceConfiguration defaultConfiguration=configuration.getInstances().get("default");
        assertEquals("http://localhost:50001", defaultConfiguration.getUrl());
        assertEquals("admin", defaultConfiguration.getUserName());
        assertEquals("110123456789abcdef0123456789abcdef", defaultConfiguration.getApiToken());

        JenkinsInstanceConfiguration otherConfiguration=configuration.getInstances().get("other");
        assertEquals("http://other:50002", otherConfiguration.getUrl());
        assertEquals("otherUser", otherConfiguration.getUserName());
        assertEquals("otherToken", otherConfiguration.getApiToken());

    }
}
