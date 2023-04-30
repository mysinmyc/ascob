package ascob.server.job;


import ascob.job.JobSpec;
import ascob.job.RunStatus;
import ascob.backend.BackendRunId;
import jakarta.persistence.*;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;

@Entity
public class InternalRun {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Lob
    @Lazy
    @Convert(converter = JobSpecConverter.class)
    JobSpec jobSpec;

    @Lob
    @Lazy
    @Convert(converter = JobSpecConverter.class)
    JobSpec runtimeSpec;


    RunStatus status;

    boolean runnable;

    @Lob
    @Convert(converter = BackendRunIdConverter.class)
    BackendRunId backendRunId;

    String submitter;

    String description;


    LocalDateTime definedTime;

    LocalDateTime submissionTime;

    LocalDateTime endTime;

    boolean monitored;
    String webhookId;

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }

    public boolean isMonitored() {
        return monitored;
    }

    public void setMonitored(boolean monitored) {
        this.monitored = monitored;
    }

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

    public boolean isRunnable() {
        return runnable;
    }

    public void setRunnable(boolean runnable) {
        this.runnable = runnable;
    }

    public JobSpec getRuntimeSpec() {
        return runtimeSpec;
    }

    public void setRuntimeSpec(JobSpec runtimeSpec) {
        this.runtimeSpec = runtimeSpec;
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getName() + " id:" + id + " status:" + status + " runnable: "+runnable+"]";
    }


}
