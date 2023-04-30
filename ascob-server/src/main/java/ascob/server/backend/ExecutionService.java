package ascob.server.backend;

import ascob.backend.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ascob.job.JobSpec;
import ascob.job.Labels;

import java.io.OutputStream;
import java.util.Map;

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

	ExecutionBackend getBackendForRun(BackendRunId backendRunId ) throws  ExecutionBackendException {
		if (backendRunId.getIdentificationKeys() == null || backendRunId.getIdentificationKeys().isEmpty()) {
			throw new ExecutionBackendException("Cannot identify run in backend");
		}
		for (ExecutionBackend currentBackend : backendRegistry.getAllBackends()) {
			if (currentBackend.getId().equals(backendRunId.getBackendId())) {
				return currentBackend;
			}
		}
		throw new ExecutionBackendException("no backend avail");
	}

	public BackendRunStatus getStatus(BackendRunId backendRunId) throws ExecutionBackendException {
		ExecutionBackend backend = getBackendForRun(backendRunId);
		try {
			return backend.getStatus(backendRunId.getIdentificationKeys());
		} catch (Exception e) {
			throw new ExecutionBackendException(e);
		}
	}

	public void writeOutputInto(BackendRunId backendRunId, OutputStream outputStream) throws ExecutionBackendException {
		ExecutionBackend backend = getBackendForRun(backendRunId);
		try {
			if ( backend instanceof BackendOutputWriter) {
				((BackendOutputWriter)backend).writeOutputInto(backendRunId.getIdentificationKeys(),outputStream);
			} else {
				throw new Exception("Backend outputs for "+backend+" not implemented");
			}
		} catch (Exception e) {
			throw new ExecutionBackendException(e);
		}
	}

	public BackendRunId updateIdentificationKeys(BackendRunId backendRunId, Map<String,String> identificationKeys) throws ExecutionBackendException {
		ExecutionBackend backend = getBackendForRun(backendRunId);
		try {
			if ( backend instanceof BackendIdentificationKeysUpdater) {
				Map<String,String> newIdentificationKeys=((BackendIdentificationKeysUpdater)backend).updateIdentificationKeys(identificationKeys, backendRunId.getIdentificationKeys());
				BackendRunId newBackendRunId = new BackendRunId();
				newBackendRunId.setBackendId(backendRunId.getBackendId());
				newBackendRunId.setIdentificationKeys(newIdentificationKeys);
				return newBackendRunId;
			} else {
				throw new Exception("Backend "+backend+" doesn't allow to set identification keys");
			}
		} catch (Exception e) {
			throw new ExecutionBackendException(e);
		}
	}

	public boolean isMonitorable(BackendRunId backendRunId) throws ExecutionBackendException{
		ExecutionBackend backend = getBackendForRun(backendRunId);
		return backend.isMonitorable(backendRunId.getIdentificationKeys());
	}
}
