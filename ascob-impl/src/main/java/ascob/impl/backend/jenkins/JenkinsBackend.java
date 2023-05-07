package ascob.impl.backend.jenkins;

import ascob.backend.*;
import ascob.impl.backend.ExecutionBackendBase;
import ascob.impl.tools.jenkins.AbortMode;
import ascob.impl.tools.jenkins.BuildResult;
import ascob.impl.tools.jenkins.JenkinsClient;
import ascob.impl.tools.jenkins.JenkinsClientManager;
import ascob.job.JobSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(matchIfMissing = false, name= "jenkins.enabled", havingValue = "true")
@Component
public class JenkinsBackend extends ExecutionBackendBase implements BackendOutputWriter, BackendIdentificationKeysUpdater, BackendJobStoppable {

	@Value("${jenkins.optimisticBuildId:true}")
	boolean optimisticBuildId;

	@Autowired
	JenkinsClientManager JenkinsClientManager;

	@Override
	public boolean isAvailableFor(JobSpec jobSpec) {
		return jobSpec.getLabels().containsKey(JenkinsLabels.JENKINS_INSTANCE);
	}

	String getJenkinsInstanceFor(JobSpec jobSpec) {
		return jobSpec.getLabels().get(JenkinsLabels.JENKINS_INSTANCE);
	}

	@Override
	public Map<String, String> submit(JobSpec jobSpec) throws Exception {
		String jenkinsInstance = getJenkinsInstanceFor(jobSpec);
		String projectName =jobSpec.getLabelValueOr(JenkinsLabels.JENKINS_PROJECT_NAME,null);
		JenkinsClient client = JenkinsClientManager.getClientByName(jenkinsInstance);
		String nextBuildId = optimisticBuildId ? client.getNextBuildNumber(projectName) : null;
		if (jobSpec.getParameters() == null || jobSpec.getParameters().isEmpty()) {
			client.build(projectName);
		} else {
			client.buildWithParameters(projectName,jobSpec.getParameters());
		}
		Map<String,String> identificationKeys = new HashMap<>();
		identificationKeys.put(JenkinsIdentificationParameters.INSTANCE, jenkinsInstance);
		identificationKeys.put(JenkinsIdentificationParameters.PROJECT_NAME, projectName);
		if (optimisticBuildId) {
			Thread.sleep(500);
			if (nextBuildId.equals( client.getLastBuildId(projectName))) {
				identificationKeys.put(JenkinsIdentificationParameters.BUILD_ID, nextBuildId);
			}
		}
		return identificationKeys;
	}

	String getBuildId(Map<String, String> identificationKeys) {
		String buildId = identificationKeys.get(JenkinsIdentificationParameters.BUILD_ID);
		if (buildId==null) {
			throw new RuntimeException("Cannot identify build id");
		}
		return buildId;
	}

	@Override
	public BackendRunStatus getStatus(Map<String, String> identificationKeys) throws Exception {
		String JenkinsInstance = identificationKeys.get(JenkinsIdentificationParameters.INSTANCE);
		JenkinsClient client = JenkinsClientManager.getClientByName(JenkinsInstance);
		BuildResult result = client.getBuildResult(identificationKeys.get(JenkinsIdentificationParameters.PROJECT_NAME), getBuildId(identificationKeys));
		if (result==null) {
			return BackendRunStatus.RUNNING;
		}
		switch (result) {
			case SUCCESS:
			return BackendRunStatus.SUCCEEDED;
			case ABORTED:
			return BackendRunStatus.ABORTED;
		default:
			return BackendRunStatus.FAILED;
		}
	}

	public void writeOutputInto(Map<String, String> identificationKeys, OutputStream outputStream) throws Exception {
		String JenkinsInstance = identificationKeys.get(JenkinsIdentificationParameters.INSTANCE);
		JenkinsClient client = JenkinsClientManager.getClientByName(JenkinsInstance);
		client.writeBuildOutputInto(identificationKeys.get(JenkinsIdentificationParameters.PROJECT_NAME), getBuildId(identificationKeys),outputStream);
	}

	@Override
	public boolean isMonitorable(Map<String, String> identificationKeys) {
		return identificationKeys !=null &&
				identificationKeys.containsKey(JenkinsIdentificationParameters.BUILD_ID) &&
				identificationKeys.containsKey(JenkinsIdentificationParameters.PROJECT_NAME) &&
				identificationKeys.containsKey(JenkinsIdentificationParameters.INSTANCE);
	}

	@Override
	public Map<String,String> updateIdentificationKeys(Map<String, String> newIdentificationKeys, Map<String, String> oldIdentificationKeys) throws Exception {
		String projectName=oldIdentificationKeys.get(JenkinsIdentificationParameters.PROJECT_NAME);
		if (!projectName.equals(newIdentificationKeys.get(JenkinsIdentificationParameters.PROJECT_NAME))) {
			throw new Exception("Cannot change project");
		}
		String oldBuildId=oldIdentificationKeys.get(JenkinsIdentificationParameters.BUILD_ID);
		if (oldBuildId !=null && ! oldBuildId.isEmpty()) {
			throw new Exception("BUILD_ID already set");
		}
		String newBuildId=newIdentificationKeys.get(JenkinsIdentificationParameters.BUILD_ID);
		if ( newBuildId == null || newBuildId.isEmpty()) {
			throw new Exception("missing BUILD_ID");
		}
		Map<String,String> result=new HashMap<>();
		result.put(JenkinsIdentificationParameters.BUILD_ID,newBuildId);
		result.put(JenkinsIdentificationParameters.PROJECT_NAME,projectName);
		result.put(JenkinsIdentificationParameters.INSTANCE,oldIdentificationKeys.get(JenkinsIdentificationParameters.INSTANCE));
		return result;
	}

	@Override
	public void stopRun(Map<String, String> identificationKeys, StopMode stopMode) throws Exception {
		String JenkinsInstance = identificationKeys.get(JenkinsIdentificationParameters.INSTANCE);
		JenkinsClient client = JenkinsClientManager.getClientByName(JenkinsInstance);
		client.abortBuild(identificationKeys.get(JenkinsIdentificationParameters.PROJECT_NAME), getBuildId(identificationKeys), getMode(stopMode));
	}

	private AbortMode getMode(StopMode stopMode) {
		switch (stopMode) {
			case CLEAN:
				return AbortMode.stop;
			default:
				return AbortMode.kill;
		}
	}
}
