package ascob.server.job;

import ascob.job.JobSpec;
import ascob.job.JobSpecBuilder;
import ascob.job.RunInfo;
import ascob.job.RunStatus;
import ascob.server.backend.ExecutionBackendException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

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
