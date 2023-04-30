package ascob.server.job;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import ascob.job.JobSpec;
import ascob.job.RunInfo;
import ascob.job.RunStatus;
import ascob.job.SubmitRequest;
import ascob.job.SubmitResponse;
import ascob.server.TestClients;
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


}
