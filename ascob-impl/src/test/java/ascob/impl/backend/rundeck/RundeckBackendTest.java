package ascob.impl.backend.rundeck;

import ascob.backend.StopMode;
import ascob.job.JobSpec;
import ascob.job.JobSpecBuilder;
import ascob.backend.BackendRunStatus;
import ascob.impl.tools.rundeck.RundeckClient;
import ascob.impl.tools.rundeck.RundeckTestContainerBuilder;
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
@ActiveProfiles("testrundeck")
public class RundeckBackendTest {

    static GenericContainer<?> rundeckContainer;

    @BeforeAll
    static void initRundeck() throws Exception {
        rundeckContainer = RundeckTestContainerBuilder.buildAndStart();
    }

    @AfterAll
    static void stopRundeck() throws Exception {
        rundeckContainer.stop();

    }

    @Test
    public void testBackend(@Autowired RundeckBackend rundeckBackend) throws Exception {

        JobSpec jobSpec = new JobSpecBuilder("test").withLabel("rundeck_instance", "default").withLabel("rundeck_job_name", "echo")
                .withLabel("rundeck_project_name", "project3")
                .withParameter("message", "ciaomiaobau").withParameter("repeat", "3").build();

        Map<String, String> identifier = rundeckBackend.submit(jobSpec);

        BackendRunStatus status = rundeckBackend.getStatus(identifier);
        assertTrue(status.equals(BackendRunStatus.RUNNING) || status.equals(BackendRunStatus.SUCCEEDED));

        for (int i = 0; i < 100 && BackendRunStatus.RUNNING.equals(rundeckBackend.getStatus(identifier)); i++) {
            Thread.sleep(500);
        }


        OutputStream outputStream = new ByteArrayOutputStream();
        rundeckBackend.writeOutputInto(identifier, outputStream);
        String outputString = outputStream.toString();
        assertEquals("ciaomiaobau\nciaomiaobau\nciaomiaobau\n", outputString);
    }


    @Test
    public void testAbort(@Autowired RundeckBackend rundeckBackend) throws Exception {

        JobSpec jobSpec = new JobSpecBuilder("test").withLabel("rundeck_instance", "default")
                .withLabel("rundeck_job_name", "sleep").withLabel("rundeck_project_name", "project3")
                .build();


        Map<String, String> identifier = rundeckBackend.submit(jobSpec);

        BackendRunStatus status = rundeckBackend.getStatus(identifier);
        assertTrue(status.equals(BackendRunStatus.RUNNING));

        rundeckBackend.stopRun(identifier, StopMode.CLEAN);
        for (int i = 0; i < 100 && BackendRunStatus.RUNNING.equals(rundeckBackend.getStatus(identifier)); i++) {
            Thread.sleep(500);
        }
        BackendRunStatus runStatus = rundeckBackend.getStatus(identifier);

        assert(BackendRunStatus.ABORTED.equals(runStatus) || BackendRunStatus.FAILED.equals(runStatus));
    }


    @Configuration
    @ComponentScan(basePackageClasses = {RundeckBackend.class, RundeckClient.class})
    static class SpringConfigurationClass {

    }
}

