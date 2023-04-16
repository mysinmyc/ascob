package ascob.server.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ascob.api.JobSpec;
import ascob.api.JobSpecBuilder;
import ascob.api.RunInfo;
import ascob.api.RunStatus;
import ascob.server.backend.ExecutionBackendException;

@SpringBootTest
public class JobServiceTest {

	@Autowired
	JobService jobService;
	
	@Test
	public void testDummyJob() throws ExecutionBackendException {
		
		JobSpec jobSpec = new JobSpecBuilder("test").withDescription("dummy").build();
		
		Long id = jobService.submit(jobSpec);
		
		RunInfo runInfo = jobService.getRunInfo(id);
		assertEquals(RunStatus.SUBMITTED, runInfo.getStatus());
		assertNotNull(runInfo.getSubmissionTime());
		assertNull(runInfo.getEndTime());
		
		
		runInfo = jobService.refresh(id);
		assertEquals(RunStatus.SUCCEDED, runInfo.getStatus());
		assertNotNull(runInfo.getEndTime());
	}
	
	@Test
	public void testSubmitException()  {
		
		JobSpec jobSpec = new JobSpecBuilder("test").build();
		
		Long id= jobService.submit(jobSpec);
		RunInfo runInfo = jobService.getRunInfo(id);
		assertEquals(RunStatus.IN_DOUBT, runInfo.getStatus());
		
	}
}
