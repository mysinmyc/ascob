package ascob.server.backend;

@SuppressWarnings("serial")
public class ExecutionBackendException extends Exception{

	public ExecutionBackendException() {
		super();
	}

	public ExecutionBackendException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExecutionBackendException(String message) {
		super(message);
	}

	public ExecutionBackendException(Throwable cause) {
		super(cause);
	}

}
