package ascob.impl.backend.rundeck;

import ascob.backend.BackendJobStoppable;
import ascob.backend.BackendOutputWriter;
import ascob.backend.BackendRunStatus;
import ascob.backend.StopMode;
import ascob.impl.backend.ExecutionBackendBase;
import ascob.impl.tools.rundeck.ExecutionStatus;
import ascob.impl.tools.rundeck.RundeckClient;
import ascob.impl.tools.rundeck.RundeckClientManager;
import ascob.job.JobSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.Map;

@ConditionalOnProperty(matchIfMissing = false, name= "rundeck.enabled", havingValue = "true")
@Component
public class RundeckBackend extends ExecutionBackendBase implements BackendOutputWriter, BackendJobStoppable {

	@Value("${rundeckBackend.output.maxLines:10000}")
	int outputMaxLines;

	@Autowired
	RundeckClientManager rundeckClientManager;

	@Override
	public boolean isAvailableFor(JobSpec jobSpec) {
		return jobSpec.getLabels().containsKey(RundeckLabels.RUNDECK_INSTANCE);
	}

	String getRundeckInstanceFor(JobSpec jobSpec) {
		return jobSpec.getLabels().get(RundeckLabels.RUNDECK_INSTANCE);
	}

	@Override
	public Map<String, String> submit(JobSpec jobSpec) throws Exception {
		String rundeckInstance = getRundeckInstanceFor(jobSpec);
		RundeckClient client = rundeckClientManager.getClientByName(rundeckInstance);
		String executionId = client.submitJobByProjectAndName(jobSpec.getLabelValueOr(RundeckLabels.RUNDECK_PROJECT_NAME,null),
				jobSpec.getLabelValueOr(RundeckLabels.RUNDECK_JOB_NAME,null), jobSpec.getParameters());
		return Map.of(RundeckIdentificationParameters.INSTANCE, rundeckInstance,
				RundeckIdentificationParameters.EXECUTION_ID, executionId);
	}

	@Override
	public BackendRunStatus getStatus(Map<String, String> identificationKeys) throws Exception {
		String rundeckInstance = identificationKeys.get(RundeckIdentificationParameters.INSTANCE);
		RundeckClient client = rundeckClientManager.getClientByName(rundeckInstance);
		ExecutionStatus status = client.getExecution(identificationKeys.get(RundeckIdentificationParameters.EXECUTION_ID)).getStatus();
		switch (status) {
		case running:
			return BackendRunStatus.RUNNING;
		case succeeded:
			return BackendRunStatus.SUCCEEDED;
		default:
			return BackendRunStatus.FAILED;
		}
	}

	public void writeOutputInto(Map<String, String> identificationKeys, OutputStream outputStream) throws Exception {
		String rundeckInstance = identificationKeys.get(RundeckIdentificationParameters.INSTANCE);
		RundeckClient client = rundeckClientManager.getClientByName(rundeckInstance);
		client.writeExecutionOutputInto(identificationKeys.get(RundeckIdentificationParameters.EXECUTION_ID),outputStream,outputMaxLines);
	}

	public void stopRun(Map<String,String> identificationKeys, StopMode stopMode) throws Exception {
		String rundeckInstance = identificationKeys.get(RundeckIdentificationParameters.INSTANCE);
		RundeckClient client = rundeckClientManager.getClientByName(rundeckInstance);
		client.abortExecution(identificationKeys.get(RundeckIdentificationParameters.EXECUTION_ID));
	}
}
