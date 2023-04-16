package ascob.api;

import java.time.LocalDateTime;

public class RunInfo {

	Long id;

	RunStatus status;

	String description;
	
	String submitter;
	
	LocalDateTime definedTime;
	
	LocalDateTime submissionTime;
	
	LocalDateTime endTime;

	
	public RunStatus getStatus() {
		return status;
	}

	public void setStatus(RunStatus status) {
		this.status = status;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSubmitter() {
		return submitter;
	}

	public void setSubmitter(String submitter) {
		this.submitter = submitter;
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

}
