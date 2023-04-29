package ascob.server.job;

import ascob.api.JobSpec;
import ascob.api.RunStatus;
import ascob.api.job.SubmitRequest;
import ascob.api.job.SubmitResponse;
import ascob.server.TestClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("webtest")
public class RunWebhookControllerTest {

    @LocalServerPort
    Integer localServerPort;

    @Autowired
    private TestClients testClients;

    @Autowired
    JobStore jobStore;

    @Test
    public void testUpdateRun() {

        SubmitRequest submitRequest = new SubmitRequest();
        submitRequest.setJobSpec(JobSpec.builder("test").withDescription("test")
                .withLabel("status", "SUBMITTED").build());
        ResponseEntity<SubmitResponse> submitResponseEntity = testClients.testUserRestTemplate().postForEntity("/api/runs", submitRequest, SubmitResponse.class);

        Long runId =submitResponseEntity.getBody().getRunId();

        assertEquals(RunStatus.SUBMITTED, jobStore.getRunById(runId).getStatus() );
        String webhookId = jobStore.getRunById(runId).getWebhookId();
        assert(webhookId!=null && ! webhookId.isEmpty());

        UpdateRunWebhookRequest updateRunRequest = new UpdateRunWebhookRequest();
        updateRunRequest.setStatus(RunStatus.SUCCEDED);

        Map<String,String> newIdentificationKeys=new HashMap<>();
        newIdentificationKeys.put("testkey", "testvalue");
        updateRunRequest.setIdentificationKeys(newIdentificationKeys);
        ResponseEntity<Object> updateRunResponseEntity = testClients.testUserRestTemplate().postForEntity("/api/webhooks/runs/"+webhookId, updateRunRequest, Object.class);

        assertTrue(updateRunResponseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(RunStatus.SUCCEDED, jobStore.getRunById(runId).getStatus() );

        InternalRun newRun =jobStore.getRunById(runId);
        assertEquals("testvalue", newRun.getBackendRunId().getIdentificationKeys().get("testkey"));
        assert(newRun.getWebhookId() ==null);

    }
}
