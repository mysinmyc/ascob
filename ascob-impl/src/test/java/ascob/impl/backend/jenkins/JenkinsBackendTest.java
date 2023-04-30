package ascob.impl.backend.jenkins;

import ascob.job.JobSpec;
import ascob.job.JobSpecBuilder;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testAbort(@Autowired JenkinsBackend jenkinsBackend) throws Exception {

        JobSpec jobSpec = new JobSpecBuilder("test").withLabel("jenkins_instance", "default").withLabel("jenkins_project_name", "sleep")
                .withParameter("seconds", "600").build();

        Map<String, String> identifier = jenkinsBackend.submit(jobSpec);

        Thread.sleep(500);
        assertEquals(BackendRunStatus.RUNNING,jenkinsBackend.getStatus(identifier));

        jenkinsBackend.stopRun(identifier);
        Thread.sleep(500);
        for (int i = 0; i < 100 && BackendRunStatus.RUNNING.equals(jenkinsBackend.getStatus(identifier)); i++) {
            Thread.sleep(500);
        }

        assertEquals(BackendRunStatus.ABORTED,jenkinsBackend.getStatus(identifier));

    }


    @Test
    public void testMonitorable(@Autowired JenkinsBackend jenkinsBackend) throws Exception {

        Map<String,String> identificationKeys = new HashMap<>();
        assertFalse(jenkinsBackend.isMonitorable(identificationKeys));
        identificationKeys.put(JenkinsIdentificationParameters.INSTANCE, "default");
        identificationKeys.put(JenkinsIdentificationParameters.PROJECT_NAME, "project1");
        identificationKeys.put(JenkinsIdentificationParameters.BUILD_ID, "1");

        assertTrue(jenkinsBackend.isMonitorable(identificationKeys));

    }


    @Test
    public void testUpdateIdentificationKeys(@Autowired JenkinsBackend jenkinsBackend) throws Exception {

        Map<String,String> oldIdentificationKeys = new HashMap<>();
        oldIdentificationKeys.put(JenkinsIdentificationParameters.PROJECT_NAME, "project1");
        oldIdentificationKeys.put(JenkinsIdentificationParameters.INSTANCE, "default");

        Map<String,String> newIdentificationKeys = new HashMap<>();
        newIdentificationKeys.put(JenkinsIdentificationParameters.BUILD_ID,"1");
        newIdentificationKeys.put(JenkinsIdentificationParameters.PROJECT_NAME, "project1");

        Map<String,String> updatedKeys= jenkinsBackend.updateIdentificationKeys(newIdentificationKeys,oldIdentificationKeys);
        assertEquals("project1", updatedKeys.get(JenkinsIdentificationParameters.PROJECT_NAME));
        assertEquals("default",updatedKeys.get(JenkinsIdentificationParameters.INSTANCE));
        assertEquals("1",updatedKeys.get(JenkinsIdentificationParameters.BUILD_ID));

        assertThrows( Throwable.class, ()-> jenkinsBackend.updateIdentificationKeys(newIdentificationKeys,updatedKeys));

        newIdentificationKeys.put(JenkinsIdentificationParameters.PROJECT_NAME, "project2");
        assertThrows( Throwable.class, ()-> jenkinsBackend.updateIdentificationKeys(newIdentificationKeys,oldIdentificationKeys));

    }


    @Configuration
    @ComponentScan(basePackageClasses = {JenkinsBackend.class, JenkinsClient.class})
    static class SpringConfigurationClass {

    }
}

