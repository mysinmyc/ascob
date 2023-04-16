package ascob.server.job;


import java.time.LocalDateTime;

import org.springframework.context.annotation.Lazy;

import ascob.api.JobSpec;
import ascob.api.RunStatus;
import ascob.backend.BackendRunId;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class InternalRun {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;
	
	@Lob
	@Lazy
	@Convert(converter = JobSpecConverter.class)
	JobSpec jobSpec;
	
	RunStatus status;
	
	
	@Lob
	@Convert(converter = BackendRunIdConverter.class)
	BackendRunId backendRunId;	

	String submitter;

	String description;


	LocalDateTime definedTime;
	
	LocalDateTime submissionTime;
	
	LocalDateTime endTime;
		
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public LocalDateTime getDefinedTime() {
		return definedTime;
	}

	public void setDefinedTime(LocalDateTime definedTime) {
		this.definedTime = definedTime;
	}

	public LocalDateTime getSubmissionTime() {
		return submissionTime;
	}

	public void setSubmissionTime(LocalDateTime submissionTime) {
		this.submissionTime = submissionTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public String getSubmitter() {
		return submitter;
	}

	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public JobSpec getJobSpec() {
		return jobSpec;
	}

	public void setJobSpec(JobSpec jobSpec) {
		this.jobSpec = jobSpec;
	}

	public RunStatus getStatus() {
		return status;
	}

	public void setStatus(RunStatus status) {
		this.status = status;
	}

	public BackendRunId getBackendRunId() {
		return backendRunId;
	}

	public void setBackendRunId(BackendRunId backendRunId) {
		this.backendRunId = backendRunId;
	}


}
