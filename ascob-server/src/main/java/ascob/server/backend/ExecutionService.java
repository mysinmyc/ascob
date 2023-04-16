package ascob.server.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ascob.api.JobSpec;
import ascob.api.Labels;
import ascob.backend.BackendRunId;
import ascob.backend.BackendRunStatus;
import ascob.backend.ExecutionBackend;

@Component
public class ExecutionService {
	
	@Autowired
	ExecutionBackendRegistry backendRegistry;
		
	static boolean filterBackend(ExecutionBackend backend,JobSpec jobSpec) {
		String backendId = jobSpec.getLabelValueOr(Labels.EXECUTION_BACKEND_ID,null);
		if (backendId ==null  || backendId.equals(backend.getId())) {
			return backend.isAvailableFor(jobSpec);
		} else {
			return false;
		}
			
	}
	
	public BackendRunId submit(JobSpec jobSpec) throws ExecutionBackendException {		
		for (ExecutionBackend currentBackend : backendRegistry.getAllBackends()) {
			if (filterBackend(currentBackend,jobSpec)) {
				try {
					BackendRunId runId = new BackendRunId();
					runId.setBackendId(currentBackend.getId());
					runId.setIdentificationKeys( currentBackend.submit(jobSpec));
					return runId;
				} catch (Exception e) {
					throw new ExecutionBackendException("An error occurred during submission",e);
				}
			}
		}		
		throw new ExecutionBackendException("no backend available for spec");
	}
		
	public BackendRunStatus getStatus(BackendRunId backendRunId) throws ExecutionBackendException {
		if (backendRunId.getIdentificationKeys() == null || backendRunId.getIdentificationKeys().isEmpty()) {
			throw new ExecutionBackendException("Cannot identify run in backend");
		}
		for (ExecutionBackend currentBackend : backendRegistry.getAllBackends()) {
			if (currentBackend.getId().equals(backendRunId.getBackendId())) {
				try {
					return currentBackend.getStatus(backendRunId.getIdentificationKeys());
				} catch (Exception e) {
					throw new ExecutionBackendException(e);
				}
			}
		}
		throw new ExecutionBackendException("no backend available for spec");
	}
}
