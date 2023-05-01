package ascob.server.job;

import ascob.backend.BackendRunId;
import ascob.backend.BackendRunStatus;
import ascob.file.FileStore;
import ascob.job.JobSpec;
import ascob.job.RunInfo;
import ascob.job.RunSearchFilters;
import ascob.job.RunStatus;
import ascob.server.backend.ExecutionBackendException;
import ascob.server.backend.ExecutionService;
import ascob.server.lock.LockManager;
import ascob.server.util.UnsafeConsumer;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class JobService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JobService.class);

	@Autowired
	JobStore jobStore;

	@Autowired
	FileStore fileStore;

	@Autowired
	ExecutionService executionService;

	@Autowired
	LockManager lockManager;

	public Long submit(JobSpec jobSpec) throws InvalidJobSpecException {
		InternalRun run = jobStore.newRun(jobSpec);
		try {
			refresh(run);
		} catch (ExecutionBackendException e) {

		}
		return run.getId();
	}

	public Long resubmitBy(Long sourceRunId,String submitter) throws  InvalidJobSpecException {
		InternalRun sourceRun = jobStore.getRunById(sourceRunId);
		if (sourceRun == null) {
			throw new JobNotFoundException();
		}
		InternalRun resubmittedRun = jobStore.duplicateRun(sourceRun,submitter);
		try {
			refresh(resubmittedRun);
		} catch (ExecutionBackendException e) {

		}
		return resubmittedRun.getId();
	}

	public boolean start(Long runId) {
		InternalRun run = jobStore.getRunById(runId);
		if (run == null) {
			throw new JobNotFoundException();
		}
		if (! (run.getRuntimeSpec().isManualStart() && run.getStatus().equals(RunStatus.DEFINED) && ! run.isRunnable() ) ) {
			return false;
		}
		run.setRunnable(true);
		jobStore.updateRun(run);
		try {
			refresh(run);
		} catch (ExecutionBackendException e) {
		}
		return true;
	}

	public void stop(Long runId) throws ExecutionBackendException {
		InternalRun run = jobStore.getRunById(runId);
		if (run == null) {
			throw new JobNotFoundException();
		}
		if (!run.getStatus().isRunning()) {
			run.setStatus(RunStatus.ABORTED);
			lockManager.releaseLocks(run.getId().toString(), run.getRuntimeSpec().getLocks());
			jobStore.updateRun(run);
		} else {
			run.setStatus(RunStatus.ABORTING);
			executionService.stopRun(run.getBackendRunId());
			jobStore.updateRun(run);
		}
	}

	public RunInfo getRunInfo(Long runId) {
		InternalRun run = jobStore.getRunById(runId);
		if (run == null) {
			throw new JobNotFoundException();
		}
		return RunInfoFactory.createRunInfo(run);
	}

	public RunInfo refresh(Long runId) throws ExecutionBackendException {
		InternalRun run = jobStore.getRunById(runId);
		if (run == null) {
			throw new JobNotFoundException();
		}
		return RunInfoFactory.createRunInfo(refresh(run));
	}

	protected InternalRun refresh(InternalRun run) throws ExecutionBackendException {
		RunStatus initialStatus = run.getStatus();
		if (initialStatus.isFinalState()) {
			return run;
		}
		if (run.isRunnable() && !initialStatus.isRunning()) {
			JobSpec jobSpec = run.getRuntimeSpec();
			if (!lockManager.acquireLocks(run.getId().toString(), jobSpec.getLocks())) {
				run.setStatus(RunStatus.WAITING_LOCKS);
				log.debug("run locked {}",run);
			} else {
				run.setSubmissionTime(LocalDateTime.now());
				try {
					BackendRunId backendRunId = executionService.submit(jobSpec);
					run.setBackendRunId(backendRunId);
					run.setMonitored(executionService.isMonitorable(backendRunId));
					run.setStatus(RunStatus.SUBMITTED);
					log.info("job submitted {} ",run);
				} catch (ExecutionBackendException e) {
					log.warn("an error occurred during submission of run "+run,e);
					run.setStatus(RunStatus.IN_DOUBT);
				}
			}
		} else {
			if (run.isMonitored()) {
				BackendRunStatus backendRunStatus = executionService.getStatus(run.getBackendRunId());
				if (backendRunStatus ==null) {
					throw new ExecutionBackendException("Something wrong in backend");
				}
				RunStatus newStatus = backendRunStatus.toRunStatus();
				run.setStatus(newStatus);
				if (newStatus.isFinalState()) {
					run.setEndTime(LocalDateTime.now());
					lockManager.releaseLocks(run.getId().toString(), run.getRuntimeSpec().getLocks());
				}
			}
		}
		if (!initialStatus.equals(run.getStatus())) {
			jobStore.updateRun(run);
		}
		return run;
	}

	public void refreshJobs() {
		refreshActiveJobs();
		refreshPendingJobs();
	}

	protected boolean refreshActiveJobs() {
		log.debug("Refresh active jobs...");
		List<InternalRun> activeRuns = jobStore.getActiveMonitoredJobs();
		for (InternalRun run : activeRuns) {
			try {
				refresh(run);
			} catch (ExecutionBackendException e) {
				log.warn("an error occurred during refresh of active job "+run,e);
			}
		}
		return ! activeRuns.isEmpty();
	}

	protected boolean refreshPendingJobs() {
		log.debug("Refresh pending jobs...");
		List<InternalRun> activeRuns = jobStore.getPendingJobs();
		for (InternalRun run : activeRuns) {
			try {
				refresh(run);
			} catch (ExecutionBackendException e) {
				log.warn("an error occurred during refresh of pending job "+run,e);
			}
		}
		return ! activeRuns.isEmpty();
	}

	public void writeRunOutputInto(Long runId, OutputStream outputStream) throws ExecutionBackendException {
		InternalRun run = jobStore.getRunById(runId);
		if (run == null) {
			throw new JobNotFoundException();
		}
		executionService.writeOutputInto(run.getBackendRunId(), outputStream);
	}


	protected <X extends Throwable> void updateRunByWebhookId(String webHookId, UnsafeConsumer<InternalRun,X> consumer) throws X {
		InternalRun internalRun = jobStore.getRunByWebhookId(webHookId);
		if (internalRun == null) {
			throw new JobNotFoundException();
		}
		consumer.accept(internalRun);
		jobStore.updateRun(internalRun);
	}

	public void updateRunStatusByWebhookId(String webHookId, RunStatus runStatus) {
		updateRunByWebhookId(webHookId, (r)->r.setStatus(runStatus));
	}

	public void updateRunBackendIdenficationKeysByWebhookId(String webHookId, Map<String,String> identificationKeys) throws  ExecutionBackendException{
		updateRunByWebhookId(webHookId, (r)->{
			BackendRunId runId = r.getBackendRunId();
			BackendRunId newRunId = executionService.updateIdentificationKeys(runId, identificationKeys);
			r.setBackendRunId(newRunId);
			r.setMonitored(executionService.isMonitorable(newRunId));
		});
	}

	public void uploadFile(Long runId, String fileId, InputStream inputStream) throws IOException {
		InternalRun internalRun = jobStore.getRunById(runId);
		if (internalRun == null) {
			throw new JobNotFoundException();
		}
		if (!internalRun.getStatus().equals(RunStatus.DEFINED)) {
			throw new RuntimeException("Invalid job status: "+internalRun.getStatus());
		}
		if (internalRun.getParentId()!=null) {
			throw new RuntimeException("Cannot add files to child jobs");
		}
		String filePath="/runs/"+runId+"/"+fileId;
		fileStore.store(filePath,inputStream);
		jobStore.addFileReference(internalRun,fileId,filePath);
	}

	public void downloadFileIntoByWebhookId(String webHookId, String fileId, OutputStream outputStream) throws IOException {
		InternalRun internalRun = jobStore.getRunByWebhookId(webHookId);
		if (internalRun == null) {
			throw new JobNotFoundException();
		}
		try {
			String filePath = jobStore.getFileReference(internalRun, fileId).getFilePath();
			fileStore.retrieveInto("/runs/" + internalRun.getId() + "/" + fileId, outputStream);
		} catch ( NoResultException e) {
			throw new FileNotFoundException();
		}
	}


	@Value("${run.search.resultLimit:20}")
	int runSearchResultLimit;

	public List<RunInfo> search(RunSearchFilters filters, int maxResults) {
		return jobStore.searchRunByConditions(
				filters,maxResults == 0 || maxResults> runSearchResultLimit ? runSearchResultLimit : maxResults).stream().map(RunInfoFactory::createRunInfo).toList();
	}

}
