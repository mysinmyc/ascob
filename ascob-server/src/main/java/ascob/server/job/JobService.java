package ascob.server.job;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ascob.api.JobSpec;
import ascob.api.RunInfo;
import ascob.api.RunStatus;
import ascob.backend.BackendRunId;
import ascob.server.backend.ExecutionBackendException;
import ascob.server.backend.ExecutionService;
import ascob.server.lock.LockManager;

@Component
public class JobService {

	@Autowired
	JobStore jobStore;

	@Autowired
	ExecutionService executionService;

	@Autowired
	LockManager lockManager;

	public Long submit(JobSpec jobSpec) {
		InternalRun run = jobStore.newRun(jobSpec);
		try { 
			refresh(run);
		} catch (ExecutionBackendException e) {
			 
		}
		return run.getId();
	}

	
	public RunInfo getRunInfo(Long runId) {
		InternalRun run =jobStore.getRunById(runId);
		if (run==null) {
			throw new JobNotFoundException();
		}
		return RunInfoFactory.createRunInfo(run);
	}
	
	public RunInfo refresh(Long runId) throws ExecutionBackendException {
		InternalRun run =jobStore.getRunById(runId);
		if (run==null) {
			throw new JobNotFoundException();
		}		
		return refresh(run);
	}

	protected RunInfo refresh(InternalRun run) throws ExecutionBackendException {
		RunStatus initialStatus = run.getStatus();
		if ( initialStatus.isFinalState()) {
			return RunInfoFactory.createRunInfo(run);
		}
		if (! initialStatus.isRunning()) {
			JobSpec jobSpec = run.getJobSpec();
			if (!lockManager.acquireLocks(run.getId().toString(), jobSpec.getLocks())) {
				run.setStatus(RunStatus.WAITING_LOCKS);
			} else {
				run.setSubmissionTime(LocalDateTime.now());
				try {
					BackendRunId backendRunId = executionService.submit(jobSpec);
					run.setBackendRunId(backendRunId);
					run.setStatus(RunStatus.SUBMITTED);
				} catch (ExecutionBackendException e) {
					run.setStatus(RunStatus.IN_DOUBT);
				}
			}
		} else {
			RunStatus newStatus= executionService.getStatus(run.getBackendRunId()).toRunStatus();
			run.setStatus(newStatus);
			if (newStatus.isFinalState()) {
				run.setEndTime(LocalDateTime.now());
				lockManager.releaseLocks(run.getId().toString(),run.getJobSpec().getLocks());
			}
		}
		if (!initialStatus.equals(run.getStatus())) {
			jobStore.updateRun(run);
		}
		return RunInfoFactory.createRunInfo(run);
	}

}
