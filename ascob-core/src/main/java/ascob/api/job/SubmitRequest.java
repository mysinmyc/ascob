package ascob.api.job;

import ascob.api.JobSpec;

public class SubmitRequest {

	
	JobSpec jobSpec;

	public JobSpec getJobSpec() {
		return jobSpec;
	}

	public void setJobSpec(JobSpec jobSpec) {
		this.jobSpec = jobSpec;
	}
}
