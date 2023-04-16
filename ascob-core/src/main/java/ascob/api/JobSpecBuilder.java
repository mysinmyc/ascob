package ascob.api;

import java.util.ArrayList;
import java.util.List;

public class JobSpecBuilder {
	JobSpec job;

	public JobSpecBuilder(String submitter) {
		job = new JobSpec();
	}
	
	public JobSpec build()  {
		return job;
	}
	
	public JobSpecBuilder withDescription(String description) {
		job.setDescription(description);
		return this;
	}
	
	public JobSpecBuilder withParameter(String name, String value) {
		JobParameters parameters = job.getParameters();
		if (parameters ==null ) {
			parameters=new JobParameters();
			job.setParameters(parameters);
		}
		parameters.put(name, value);
		return this;
	}
	
	public JobSpecBuilder withLabel(String name, String value) {
		JobLabels labels = job.getLabels();
		if (labels ==null ) {
			labels=new JobLabels();
			job.setLabels(labels);
		}
		labels.put(name, value);
		return this;
	}	
	
	public JobSpecBuilder withLock(LockSpec lock) {
		List<LockSpec> locks = job.getLocks();
		if (locks ==null ) {
			locks=new ArrayList<LockSpec>();
			job.setLocks(locks);
		}
		locks.add(lock);
		return this;
	}	
}
