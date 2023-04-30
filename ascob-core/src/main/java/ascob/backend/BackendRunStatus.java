package ascob.backend;

import ascob.job.RunStatus;

public enum BackendRunStatus {
	RUNNING,
	SUCCEDED,
	FAILED,
	ABORTED;
	
	public RunStatus toRunStatus() {
		switch (this) {
			case SUCCEDED:
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
