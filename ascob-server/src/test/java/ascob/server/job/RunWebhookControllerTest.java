package ascob.server.job;

import ascob.job.*;
import ascob.server.TestClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        ResponseEntity<SubmitResponse> submitResponseEntity = testClients.withRootToken().postForEntity("/api/runs", submitRequest, SubmitResponse.class);

        Long runId =submitResponseEntity.getBody().getRunId();

        assertEquals(RunStatus.SUBMITTED, jobStore.getRunById(runId).getStatus() );
        String webhookId = jobStore.getRunById(runId).getWebhookId();
        assert(webhookId!=null && ! webhookId.isEmpty());

        UpdateRunWebhookRequest updateRunRequest = new UpdateRunWebhookRequest();
        updateRunRequest.setStatus(RunStatus.SUCCEDED);

        Map<String,String> newIdentificationKeys=new HashMap<>();
        newIdentificationKeys.put("testkey", "testvalue");
        updateRunRequest.setIdentificationKeys(newIdentificationKeys);

        assertThrows(RestClientException.class, ()->testClients.withoutPrivilegesToken().postForObject("/api/webhooks/runs/"+webhookId, updateRunRequest, Object.class));

        ResponseEntity<Object> updateRunResponseEntity = testClients.withWebhookToken().postForEntity("/api/webhooks/runs/"+webhookId, updateRunRequest, Object.class);

        assertTrue(updateRunResponseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(RunStatus.SUCCEDED, jobStore.getRunById(runId).getStatus() );

        InternalRun newRun =jobStore.getRunById(runId);
        assertEquals("testvalue", newRun.getBackendRunId().getIdentificationKeys().get("testkey"));
        assert(newRun.getWebhookId() ==null);

    }
}
