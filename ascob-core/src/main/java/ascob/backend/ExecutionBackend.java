package ascob.backend;

import java.util.Map;

import ascob.api.JobSpec;

public interface ExecutionBackend {

	String getId();
	
	boolean isAvailableFor( JobSpec jobSpec);
	
	Map<String,String> submit(JobSpec jobSpec) throws Exception;
	
	BackendRunStatus getStatus(Map<String,String> identificationKeys) throws Exception;
}
