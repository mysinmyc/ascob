package ascob.api;

import java.util.List;

public class JobSpec {

	String description;


	String submitter;

	JobLabels labels;
	
	JobParameters parameters;

	List<LockSpec> locks;
		
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<LockSpec> getLocks() {
		return locks;
	}

	public void setLocks(List<LockSpec> locks) {
		this.locks = locks;
	}

	public String getLabelValueOr(String label, String defaultValue) {
		if (labels==null) {
			return defaultValue;
		}
		String value=labels.get(label);
		return value==null || value.isEmpty() ? defaultValue : value;
	}
	
	public JobLabels getLabels() {
		return labels;
	}

	public void setLabels(JobLabels labels) {
		this.labels = labels;
	}

	public JobParameters getParameters() {
		return parameters;
	}

	public void setParameters(JobParameters parameters) {
		this.parameters = parameters;
	}
		
	public String getSubmitter() {
		return submitter;
	}

	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}
	
	
	public static JobSpecBuilder builder(String submitter) {
		return new JobSpecBuilder(submitter);
	}
}
