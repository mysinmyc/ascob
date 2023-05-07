package ascob.backend;

import ascob.job.RunStatus;

/**
 * Status of run in the backend. It has been defined to simplify run status to backend implementers by hiding unuseful statuses
 */
public enum BackendRunStatus {

	/**
	 * Job is running in the backend
	 */
	RUNNING,

	/**
	 * Job completed successfully
	 */
	SUCCEEDED,

	/**
	 * Job failed in the backend
	 */
	FAILED,

	/**
	 * Job aborted
	 */
	ABORTED;


	/***
	 * Convert Status of run in the backend to Job Run Status
	 * @return RunStatus
	 */
	public RunStatus toRunStatus() {
		switch (this) {
			case SUCCEEDED:
				return RunStatus.SUCCEDED;
			case FAILED:
				return RunStatus.FAILED;
			case ABORTED:
				return RunStatus.ABORTED;
			default:
				return RunStatus.RUNNING;
		}
	}
}
