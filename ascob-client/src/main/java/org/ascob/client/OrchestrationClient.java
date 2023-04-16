package org.ascob.client;


import ascob.api.JobSpecBuilder;
import ascob.api.JobSpec;
import ascob.api.RunInfo;

public class OrchestrationClient {

	
	public OrchestrationClient(String address, String token) {
		
	}
	
		
	public JobSpecBuilder newJob(String submitter) {
		return new JobSpecBuilder(submitter);
	}
	
	public Long submit(JobSpec jobSpec) {
		return null;
	}
	
	public RunInfo getRunInfo(Long runId) {
		return null;
	}
	
	public RunInfo refresh(Long runId) {
		return null;
	}
}
