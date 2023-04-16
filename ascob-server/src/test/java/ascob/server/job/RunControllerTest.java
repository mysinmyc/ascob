package ascob.server.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import ascob.api.JobSpec;
import ascob.api.RunInfo;
import ascob.api.RunStatus;
import ascob.api.job.SubmitRequest;
import ascob.api.job.SubmitResponse;
import ascob.server.TestClients;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("webtest")
public class RunControllerTest {

	@LocalServerPort
	Integer localServerPort;
	
	@Autowired
	private TestClients testClients;
	
	@Test
	public void testSubmit() {
		
		SubmitRequest submitRequest = new SubmitRequest();
		submitRequest.setJobSpec(JobSpec.builder("test").withDescription("dummy").build());
		ResponseEntity<SubmitResponse> submitResponseEntity =testClients.testUserRestTemplate().postForEntity("/api/runs",submitRequest, SubmitResponse.class);
		assertTrue( submitResponseEntity.getStatusCode().is2xxSuccessful());
		
		ResponseEntity<RunInfo> getRunInfoResponse =testClients.testUserRestTemplate().getForEntity("/api/runs/"+submitResponseEntity.getBody().getRunId(), RunInfo.class);
		assertTrue( getRunInfoResponse.getStatusCode().is2xxSuccessful());
		assertEquals(RunStatus.SUBMITTED, getRunInfoResponse.getBody().getStatus());
		
		ResponseEntity<RunInfo> refreshResponse=testClients.testUserRestTemplate().getForEntity("/api/runs/"+submitResponseEntity.getBody().getRunId()+"/refresh",RunInfo.class);
		assertTrue( refreshResponse.getStatusCode().is2xxSuccessful());
		assertEquals(RunStatus.SUCCEDED, refreshResponse.getBody().getStatus());
		

	}


}
