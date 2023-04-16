package ascob.backend;

import ascob.api.RunStatus;

public enum BackendRunStatus {
	RUNNING,
	SUCCEDED,
	FAILED;
	
	public RunStatus toRunStatus() {
		switch (this) {
			case SUCCEDED:
				return RunStatus.SUCCEDED;
			case FAILED:
				return RunStatus.FAILED;
			default:
				return RunStatus.RUNNING;
		}
	}
}
