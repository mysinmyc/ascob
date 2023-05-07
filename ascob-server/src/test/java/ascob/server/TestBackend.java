package ascob.server;

import ascob.backend.*;
import ascob.job.JobSpec;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class TestBackend  implements ExecutionBackend, BackendIdentificationKeysUpdater, BackendJobStoppable {

	@Override
	public String getId() {
		return "_test";
	}

	@Override
	public boolean isAvailableFor(JobSpec jobSpec) {
		return jobSpec.getDescription() !=null&& jobSpec.getDescription().toLowerCase().contains("test");
	}

	Map<String,BackendRunStatus> jobsStatus = new HashMap<String, BackendRunStatus>();
	@Override
	public Map<String, String> submit(JobSpec jobSpec) throws Exception {
		if ("true".equals(jobSpec.getLabelValueOr("fail", "false"))) {
			throw new Exception("KO");
		}
		HashMap<String,String> ids = new HashMap<>();
		String jobId=UUID.randomUUID().toString();
		ids.put("id", jobId);
		jobsStatus.put(jobId,  BackendRunStatus.valueOf(jobSpec.getLabelValueOr("status",  BackendRunStatus.SUCCEEDED.toString())));
		return ids;
	}

	@Override
	public BackendRunStatus getStatus(Map<String, String> identificationKeys) throws Exception {
		return jobsStatus.get(identificationKeys.get("id"));
	}

	@Override
	public Map<String, String> updateIdentificationKeys(Map<String, String> newIdentificationKeys, Map<String, String> oldIdentificationKeys) throws Exception {
		return newIdentificationKeys;
	}

	@Override
	public void stopRun(Map<String, String> identificationKeys, StopMode stopMode) throws Exception {
		jobsStatus.put(identificationKeys.get("id"), BackendRunStatus.ABORTED);
	}
}
