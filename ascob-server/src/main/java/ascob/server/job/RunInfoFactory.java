package ascob.server.job;

import ascob.job.RunInfo;

public class RunInfoFactory {

	public static  RunInfo createRunInfo(InternalRun run ) {
		RunInfo runInfo = new RunInfo();
		runInfo.setDefinedTime(run.getDefinedTime());
		runInfo.setDescription(run.getDescription());
		runInfo.setEndTime(run.getEndTime());
		runInfo.setId(run.getId());
		runInfo.setStatus(run.getStatus());
		runInfo.setSubmissionTime(run.getSubmissionTime());
		runInfo.setSubmitter(run.getSubmitter());
		return runInfo;
	}
}
