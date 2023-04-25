package ascob.api;

public enum RunStatus {

	DEFINED(false,false),
	WAITING_LOCKS(false,false),
	SUBMITTED(true,false),
	IN_DOUBT(false,true),
	RUNNING(true,false),
	SUCCEDED(false,true),
	FAILED(false,true),
	ABORTED(false,true);
	
	boolean running;
	boolean finalState;
	
	RunStatus(boolean running, boolean finalState) {
		this.running=running;
		this.finalState = finalState;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public boolean isFinalState() {
		return finalState;
	}
	
}
