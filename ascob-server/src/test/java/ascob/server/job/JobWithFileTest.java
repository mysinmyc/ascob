package ascob.server.job;

import ascob.job.JobSpec;
import ascob.job.RunInfo;
import ascob.job.RunStatus;
import ascob.job.SubmitRequest;
import ascob.job.SubmitResponse;
import ascob.file.FileStore;
import ascob.server.TestClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("webtest")
public class JobWithFileTest {

    @Autowired
    FileStore fileStore;

    @Autowired
    TestClients testClients;

    @Autowired
    JobStore jobStore;

    @Test
    public void testJobWithFile() throws Exception{

        ClassPathResource testFile = new ClassPathResource("testFile.bin");

        SubmitRequest submitRequest = new SubmitRequest();
        submitRequest.setJobSpec(JobSpec.builder("test").withDescription("dummy").withManualStart().build());

        assertThrows(RestClientException.class, ()->testClients.withoutPrivilegesToken().postForObject("/api/runs",submitRequest, Object.class));

        ResponseEntity<SubmitResponse> submitResponseEntity =testClients.withJobManagerToken().postForEntity("/api/runs",submitRequest, SubmitResponse.class);
        assertTrue( submitResponseEntity.getStatusCode().is2xxSuccessful());

        long runId=submitResponseEntity.getBody().getRunId();
        assertEquals(RunStatus.DEFINED, testClients.withJobManagerToken().getForObject("/api/runs/"+runId, RunInfo.class).getStatus());

        ResponseEntity<Object> uploadFileResponse = testClients.withJobManagerToken().postForEntity("/api/runs/"+runId+"/files/file1",
                testFile,
                Object.class);

        assertTrue(uploadFileResponse.getStatusCode().is2xxSuccessful());

        assertTrue(fileStore.exists("/runs/"+submitResponseEntity.getBody().getRunId()+"/file1"));

        ByteArrayOutputStream storedFile = new ByteArrayOutputStream();
        fileStore.retrieveInto("/runs/"+runId+"/file1",storedFile);
        assertArrayEquals(testFile.getContentAsByteArray(),  storedFile.toByteArray());

        String webhookId = jobStore.getRunById(runId).getWebhookId();

        ResponseEntity<byte[]> downloadedFileEntity = testClients.withWebhookToken().getForEntity("/api/webhooks/runs/"+webhookId+"/files/file1",
                byte[].class);

        assertTrue(downloadedFileEntity.getStatusCode().is2xxSuccessful());
        assertArrayEquals(testFile.getContentAsByteArray(),  downloadedFileEntity.getBody());

    }
}
