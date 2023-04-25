package ascob.impl.backend.jenkins;

import ascob.api.JobSpec;
import ascob.api.JobSpecBuilder;
import ascob.backend.BackendRunStatus;
import ascob.impl.tools.jenkins.JenkinsClient;
import ascob.impl.tools.jenkins.JenkinsTestContainerBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("testjenkins")
public class JenkinsBackendTest {

    static GenericContainer<?> jenkinsContainer;

    @BeforeAll
    static void initRundeck() throws Exception {
        jenkinsContainer = JenkinsTestContainerBuilder.buildAndStart(50001);
    }

    @AfterAll
    static void stopRundeck() throws Exception {
        jenkinsContainer.stop();

    }

    @Test
    public void testBackend(@Autowired JenkinsBackend jenkinsBackend) throws Exception {

        JobSpec jobSpec = new JobSpecBuilder("test").withLabel("jenkins_instance", "default").withLabel("jenkins_project_name", "echo")
                .withParameter("message", "ciaomiaobau").build();

        Map<String, String> identifier = jenkinsBackend.submit(jobSpec);
        Thread.sleep(500);
        for (int i = 0; i < 100 && BackendRunStatus.RUNNING.equals(jenkinsBackend.getStatus(identifier)); i++) {
            Thread.sleep(500);
        }
        BackendRunStatus status = jenkinsBackend.getStatus(identifier);
        assertEquals(BackendRunStatus.SUCCEDED, status );

        OutputStream outputStream = new ByteArrayOutputStream();
        jenkinsBackend.writeOutputInto(identifier, outputStream);
        String outputString = outputStream.toString();
        assertTrue(outputString.contains("+ echo ciaomiaobau"));
    }

    @Configuration
    @ComponentScan(basePackageClasses = {JenkinsBackend.class, JenkinsClient.class})
    static class SpringConfigurationClass {

    }
}

