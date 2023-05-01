package ascob.server.job;

import ascob.backend.BackendRunStatus;
import ascob.job.*;
import ascob.server.TestClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("webtest")
public class RunControllerTest {


	@Autowired
	private TestClients testClients;


	@Test
	public void testSubmit() {
		
		SubmitRequest submitRequest = new SubmitRequest();
		submitRequest.setJobSpec(JobSpec.builder("test").withDescription("dummy").build());

		assertThrows(RestClientException.class, ()->testClients.withoutPrivilegesToken().postForObject("/api/runs",submitRequest, Object.class));

		ResponseEntity<SubmitResponse> submitResponseEntity =testClients.withJobManagerToken().postForEntity("/api/runs",submitRequest, SubmitResponse.class);
		assertTrue( submitResponseEntity.getStatusCode().is2xxSuccessful());

		assertThrows(RestClientException.class, ()->  testClients.withoutPrivilegesToken().getForObject("/api/runs/"+submitResponseEntity.getBody().getRunId(), RunInfo.class));


		ResponseEntity<RunInfo> getRunInfoResponse =testClients.withJobManagerToken().getForEntity("/api/runs/"+submitResponseEntity.getBody().getRunId(), RunInfo.class);
		assertTrue( getRunInfoResponse.getStatusCode().is2xxSuccessful());
		assertEquals(RunStatus.SUBMITTED, getRunInfoResponse.getBody().getStatus());

		assertEquals(401,testClients.withoutPrivilegesToken().getForEntity("/api/runs/"+submitResponseEntity.getBody().getRunId()+"/refresh", Object.class).getStatusCode().value());

		ResponseEntity<RunInfo> refreshResponse=testClients.withJobManagerToken().getForEntity("/api/runs/"+submitResponseEntity.getBody().getRunId()+"/refresh",RunInfo.class);
		assertTrue( refreshResponse.getStatusCode().is2xxSuccessful());
		assertEquals(RunStatus.SUCCEDED, refreshResponse.getBody().getStatus());

		assertEquals(401,testClients.withoutPrivilegesToken().getForEntity("/api/runs/"+submitResponseEntity.getBody().getRunId()+"/output.txt", Object.class).getStatusCode().value());

		String output = testClients.withJobManagerToken().getForObject("/api/runs/"+submitResponseEntity.getBody().getRunId()+"/output.txt",String.class);
		assertEquals("Dummy output", output);
	}


	@Test
	public void testResubmit() {

		SubmitRequest submitRequest = new SubmitRequest();
		submitRequest.setJobSpec(JobSpec.builder("user1").withDescription("dummy job").withManualStart().build());

		ResponseEntity<SubmitResponse> submitResponseEntity =testClients.withJobManagerToken().postForEntity("/api/runs",submitRequest, SubmitResponse.class);
		assertTrue(submitResponseEntity.getStatusCode().is2xxSuccessful());

		ResponseEntity<RunInfo> getSubmittedRunInfoResponse =testClients.withJobManagerToken().getForEntity("/api/runs/"+submitResponseEntity.getBody().getRunId(), RunInfo.class);
		assertEquals(RunStatus.DEFINED, getSubmittedRunInfoResponse.getBody().getStatus());

		ResponseEntity<SubmitResponse> resubmitResponse =testClients.withJobManagerToken().getForEntity("/api/runs/"+submitResponseEntity.getBody().getRunId()+"/resubmit?submitter=user2",SubmitResponse.class);
		assertTrue( submitResponseEntity.getStatusCode().is2xxSuccessful());

		ResponseEntity<RunInfo> getResubmittedRunInfoResponse =testClients.withJobManagerToken().getForEntity("/api/runs/"+resubmitResponse.getBody().getRunId(), RunInfo.class);
		assertTrue( getResubmittedRunInfoResponse.getStatusCode().is2xxSuccessful());
		assertEquals("user2", getResubmittedRunInfoResponse.getBody().getSubmitter());
		assertEquals("dummy job", getResubmittedRunInfoResponse.getBody().getDescription());
		assertEquals(submitResponseEntity.getBody().getRunId(), getResubmittedRunInfoResponse.getBody().getParentId());
		assertEquals(RunStatus.SUBMITTED, getResubmittedRunInfoResponse.getBody().getStatus());

	}


	@Test
	public void testAbort() {

		SubmitRequest submitRequest = new SubmitRequest();
		submitRequest.setJobSpec(JobSpec.builder("test").withDescription("test").withLocks("test1").withLabel("status", BackendRunStatus.RUNNING.name()).withDescription("test").build());

		ResponseEntity<SubmitResponse> submitResponseEntity =testClients.withJobManagerToken().postForEntity("/api/runs",submitRequest, SubmitResponse.class);
		assertTrue(submitResponseEntity.getStatusCode().is2xxSuccessful());

		ResponseEntity<RunInfo> getSubmittedRunInfoResponse =testClients.withJobManagerToken().getForEntity("/api/runs/"+submitResponseEntity.getBody().getRunId()+"/refresh", RunInfo.class);
		assertEquals(RunStatus.RUNNING, getSubmittedRunInfoResponse.getBody().getStatus());


		testClients.withJobManagerToken().delete("/api/runs/"+submitResponseEntity.getBody().getRunId());

		getSubmittedRunInfoResponse =testClients.withJobManagerToken().getForEntity("/api/runs/"+submitResponseEntity.getBody().getRunId(), RunInfo.class);
		assertEquals(RunStatus.ABORTING, getSubmittedRunInfoResponse.getBody().getStatus());

		getSubmittedRunInfoResponse =testClients.withJobManagerToken().getForEntity("/api/runs/"+submitResponseEntity.getBody().getRunId()+"/refresh", RunInfo.class);
		assertEquals(RunStatus.ABORTED, getSubmittedRunInfoResponse.getBody().getStatus());
	}
}
