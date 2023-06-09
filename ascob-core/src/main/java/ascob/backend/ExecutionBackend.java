package ascob.backend;

import ascob.job.JobSpec;

import java.util.Map;

public interface ExecutionBackend {

	String getId();
	
	boolean isAvailableFor( JobSpec jobSpec);
	
	Map<String,String> submit(JobSpec jobSpec) throws Exception;
	
	BackendRunStatus getStatus(Map<String,String> identificationKeys) throws Exception;

	default boolean isMonitorable(Map<String,String> identificationKeys) {
		return identificationKeys!=null && ! identificationKeys.isEmpty();
	}
}
