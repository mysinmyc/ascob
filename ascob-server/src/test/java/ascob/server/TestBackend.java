package ascob.server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ascob.backend.BackendIdentificationKeysUpdater;
import org.springframework.stereotype.Component;

import ascob.api.JobSpec;
import ascob.backend.BackendRunStatus;
import ascob.backend.ExecutionBackend;

@Component
public class TestBackend  implements ExecutionBackend, BackendIdentificationKeysUpdater {

	@Override
	public String getId() {
		return "_test";
	}

	@Override
	public boolean isAvailableFor(JobSpec jobSpec) {
		return jobSpec.getDescription() !=null&& jobSpec.getDescription().toLowerCase().contains("test");
	}

	@Override
	public Map<String, String> submit(JobSpec jobSpec) throws Exception {
		if ("true".equals(jobSpec.getLabelValueOr("fail", "false"))) {
			throw new Exception("KO");
		}
		HashMap<String,String> ids = new HashMap<>();
		ids.put("id", UUID.randomUUID().toString());
		ids.put("status", jobSpec.getLabelValueOr("status",  BackendRunStatus.SUCCEDED.toString()));
		return ids;
	}

	@Override
	public BackendRunStatus getStatus(Map<String, String> identificationKeys) throws Exception {
		String status = identificationKeys.get("status");
		if (status==null || status.isEmpty()) {
			throw new Exception("KO");
		}
		return BackendRunStatus.valueOf(status);
	}

	@Override
	public Map<String, String> updateIdentificationKeys(Map<String, String> newIdentificationKeys, Map<String, String> oldIdentificationKeys) throws Exception {
		return newIdentificationKeys;
	}
}
