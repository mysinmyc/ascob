package ascob.impl.tools.jenkins;

import ascob.impl.tools.jenkins.JenkinsClient;
import ascob.impl.tools.jenkins.JenkinsClientManager;
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
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("testjenkins")
public class JenkinsClientTest {

    static GenericContainer<?> jenkinsContainer;

    @BeforeAll
    static void initJenkins() throws Exception {
        jenkinsContainer = JenkinsTestContainerBuilder.buildAndStart(50001);
    }

    @AfterAll
    static void stopJenkins() throws  Exception{
        jenkinsContainer.stop();
    }

    @Test
    public void testClient(@Autowired JenkinsClientManager JenkinsClientManager) throws InterruptedException, IOException {

        JenkinsClient jenkinsClient = JenkinsClientManager.getClientByName("default");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("message", "ciaomiaobau");
        jenkinsClient.buildWithParameters("echo", parameters);
        Thread.sleep(500);
        String buildId = jenkinsClient.getLastBuildId("echo");
        for (int i = 0; i < 100 && jenkinsClient.getBuildResult("echo", buildId) == null; i++) {
            Thread.sleep(500);
        }
        BuildResult buildResult = jenkinsClient.getBuildResult("echo", buildId);
        assertEquals(BuildResult.SUCCESS, buildResult);

        OutputStream outputStream = new ByteArrayOutputStream();
        jenkinsClient.writeBuildOutputInto("echo", buildId, outputStream);
        String outputString = outputStream.toString();
        assertTrue(outputString.contains("+ echo ciaomiaobau"));
    }

    @Test
    public void testJobko(@Autowired JenkinsClientManager JenkinsClientManager) throws InterruptedException, IOException {

        JenkinsClient jenkinsClient = JenkinsClientManager.getClientByName("default");

        jenkinsClient.build("jobKo");
        Thread.sleep(500);
        String jobKoBuildId = jenkinsClient.getLastBuildId("jobKo");
        for ( int i=0;i<100 && jenkinsClient.getBuildResult("jobKo", jobKoBuildId)==null;i++) {
            Thread.sleep(500);
        }
        BuildResult jobKoBuildResult = jenkinsClient.getBuildResult("jobKo", jobKoBuildId);
        assertEquals(BuildResult.FAILURE, jobKoBuildResult);
    }

    @Test
    public void testAbort (@Autowired JenkinsClientManager JenkinsClientManager) throws InterruptedException, IOException {

        JenkinsClient jenkinsClient = JenkinsClientManager.getClientByName("default");

        Map<String,String> parameters = new HashMap<>();
        parameters.put("seconds", "500");
        jenkinsClient.buildWithParameters("sleep", parameters);
        Thread.sleep(500);
        String jobBuildId = jenkinsClient.getLastBuildId("sleep");

        BuildResult jobBuildResult = jenkinsClient.getBuildResult("sleep", jobBuildId);
        assertNull(jobBuildResult);

        jenkinsClient.abortBuild("sleep", jobBuildId);
        for ( int i=0;i<100 && jenkinsClient.getBuildResult("sleep", jobBuildId)==null;i++) {
            Thread.sleep(500);
        }
        jobBuildResult = jenkinsClient.getBuildResult("sleep", jobBuildId);
        assertEquals(BuildResult.ABORTED, jobBuildResult);

    }

    @Configuration
    @ComponentScan
    static class SpringConfigurationClass {

    }
}
