package ascob.server.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
	
	
	@Test 
	public void testRefreshJobs() {

		JobSpec jobOkSpec = new JobSpecBuilder("test").withDescription("dummy").build();
		JobSpec jobKoSpec = new JobSpecBuilder("test").build();
		Long idOk1= jobService.submit(jobOkSpec);
		Long idKo= jobService.submit(jobKoSpec);
		Long idOk2= jobService.submit(jobOkSpec);
		
		boolean resultRefresh1 =jobService.refreshActiveJobs();
		assertTrue(resultRefresh1);
		
		assertEquals(RunStatus.SUCCEDED, jobService.getRunInfo(idOk1).getStatus());
		assertEquals(RunStatus.IN_DOUBT, jobService.getRunInfo(idKo).getStatus());
		assertEquals(RunStatus.SUCCEDED, jobService.getRunInfo(idOk2).getStatus());

		boolean resultRefresh2 =jobService.refreshActiveJobs();
		assertFalse(resultRefresh2);

	}
}
