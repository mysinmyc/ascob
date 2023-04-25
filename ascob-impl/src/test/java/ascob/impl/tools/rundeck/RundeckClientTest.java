package ascob.impl.tools.rundeck;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("testrundeck")
public class RundeckClientTest {

    static GenericContainer<?> rundeckContainer;

    @BeforeAll
    static void initRundeck() throws Exception {
        rundeckContainer = RundeckTestContainerBuilder.buildAndStart();
    }

    @AfterAll
    static void stopRundeck() {
        rundeckContainer.stop();
    }

    @Test
    public void testClient(@Autowired RundeckClientManager rundeckClientManager) throws InterruptedException, IOException {

        RundeckClient rundeckClient = rundeckClientManager.getClientByName("default");

        Map<String, String> options = new HashMap<>();
        options.put("message", "ciaomiaobau");
        options.put("repeat", "3");
        String jobId = rundeckClient.submitJobByProjectAndName("project3", "echo", options);

        ExecutionStatus status = rundeckClient.getExecution(jobId).getStatus();
        assertTrue(status.equals(ExecutionStatus.running) || status.equals(ExecutionStatus.succeeded));

        for (int i = 0; i < 100 && ExecutionStatus.running.equals(rundeckClient.getExecution(jobId).getStatus()); i++) {
            Thread.sleep(500);
        }


        OutputStream outputStream = new ByteArrayOutputStream();
        rundeckClient.writeExecutionOutputInto(jobId, outputStream, 100000);
        String outputString = outputStream.toString();
        assertEquals("ciaomiaobau\nciaomiaobau\nciaomiaobau\n", outputString);


        String sleepingJobId = rundeckClient.submitJobByProjectAndName("project3", "sleep",null);
        ExecutionStatus sleepingJobStatus = rundeckClient.getExecution(sleepingJobId).getStatus();
        assertTrue(sleepingJobStatus.equals(ExecutionStatus.running));
        rundeckClient.abortExecution(sleepingJobId);

        for (int i = 0; i < 100 && ExecutionStatus.running.equals(rundeckClient.getExecution(sleepingJobId).getStatus()); i++) {
            Thread.sleep(500);
        }
        sleepingJobStatus = rundeckClient.getExecution(sleepingJobId).getStatus();
        assert(ExecutionStatus.aborted.equals(sleepingJobStatus) || ExecutionStatus.failed.equals(sleepingJobStatus));
    }

    @Configuration
    @ComponentScan
    static class SpringConfigurationClass {

    }
}
