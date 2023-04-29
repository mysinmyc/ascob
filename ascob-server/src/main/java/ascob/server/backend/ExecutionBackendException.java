package ascob.server.backend;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Backend exception")
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
