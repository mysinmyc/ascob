package ascob.server.job;

import ascob.backend.BackendRunStatus;
import ascob.job.*;
import ascob.server.backend.ExecutionBackendException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JobServiceTest {

	@Autowired
	JobService jobService;

	@Autowired
	JobStore jobStore;

	@Test
	public void testDummyJob() throws ExecutionBackendException, InvalidJobSpecException {

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
	public void testSubmitException() throws InvalidJobSpecException {

		JobSpec jobSpec = new JobSpecBuilder("test").build();

		Long id = jobService.submit(jobSpec);
		RunInfo runInfo = jobService.getRunInfo(id);
		assertEquals(RunStatus.IN_DOUBT, runInfo.getStatus());

	}


	@Test
	public void testRefreshJobs() throws InvalidJobSpecException {

		JobSpec jobOkSpec = new JobSpecBuilder("test").withDescription("dummy").build();
		JobSpec jobKoSpec = new JobSpecBuilder("test").build();
		Long idOk1 = jobService.submit(jobOkSpec);
		Long idKo = jobService.submit(jobKoSpec);
		Long idOk2 = jobService.submit(jobOkSpec);

		boolean resultRefresh1 = jobService.refreshActiveJobs();
		assertTrue(resultRefresh1);

		assertEquals(RunStatus.SUCCEDED, jobService.getRunInfo(idOk1).getStatus());
		assertEquals(RunStatus.IN_DOUBT, jobService.getRunInfo(idKo).getStatus());
		assertEquals(RunStatus.SUCCEDED, jobService.getRunInfo(idOk2).getStatus());

		boolean resultRefresh2 = jobService.refreshActiveJobs();
		assertFalse(resultRefresh2);

	}


	@Test
	public void testVariablesResolution() throws InvalidJobSpecException {

		JobSpec jobSpec = JobSpec.builder("paperino").withParameter("submittedBy", "%%SUBMITTER%%").withParameter("webhookId", "%%WEBHOOKID%%")
				.withDescription("Dummy job submitted by %%SUBMITTER%% with runId %%RUNID%%").enableRuntimeVariables().build();

		long runId = jobService.submit(jobSpec);

		InternalRun run = jobStore.getRunById(runId);
		JobSpec runtimeSpec = run.getRuntimeSpec();

		assertEquals("paperino", runtimeSpec.getParameters().get("submittedBy"));
		assertEquals(run.getWebhookId(), runtimeSpec.getParameters().get("webhookId"));
		assertEquals("Dummy job submitted by paperino with runId "+run.getId(), runtimeSpec.getDescription());

		JobSpec jobSpecKo = JobSpec.builder("ciao").enableRuntimeVariables().withDescription("dummy %%ciao%%").build();

		assertThrows(InvalidJobSpecException.class, () -> jobService.submit(jobSpecKo));
	}

	@Test
	public void testManualStart() throws InvalidJobSpecException {

		JobSpec jobSpec = JobSpec.builder("test").withManualStart().withDescription("test").build();
		long runId = jobService.submit(jobSpec);

		jobService.refreshActiveJobs();
		RunInfo runInfo = jobService.getRunInfo(runId);
		assertEquals(RunStatus.DEFINED, runInfo.getStatus());

		jobService.start(runId);

		RunInfo runInfoAfterStart = jobService.getRunInfo(runId);
		assertEquals(RunStatus.SUBMITTED, runInfoAfterStart.getStatus());

	}

	@Test
	public void testLock() throws InvalidJobSpecException, ExecutionBackendException {

		JobSpec jobSpecBlocker = JobSpec.builder("test").withDescription("test").withLocks("test1").withLabel("status", BackendRunStatus.RUNNING.name()).withDescription("test").build();
		long runIdBlocker = jobService.submit(jobSpecBlocker);

		jobService.refreshJobs();

		RunInfo runInfoBlocker = jobService.getRunInfo(runIdBlocker);
		assertEquals(RunStatus.RUNNING, runInfoBlocker.getStatus());

		JobSpec jobSpecBlocked = JobSpec.builder("test").withDescription("test").withLocks("test1").withLabel("status", BackendRunStatus.RUNNING.name()).withDescription("test").build();
		long runIdBlocked = jobService.submit(jobSpecBlocked);

		jobService.refreshJobs();

		RunInfo runInfoBlocked = jobService.getRunInfo(runIdBlocked);
		assertEquals(RunStatus.WAITING_LOCKS, runInfoBlocked.getStatus());

		jobService.stop(runIdBlocker);

		jobService.refreshJobs();

		runInfoBlocker = jobService.getRunInfo(runIdBlocker);
		assertEquals(RunStatus.ABORTED, runInfoBlocker.getStatus());

		jobService.refresh(runIdBlocked);
		runInfoBlocked = jobService.getRunInfo(runIdBlocked);
		assertEquals(RunStatus.RUNNING, runInfoBlocked.getStatus());


	}
}
