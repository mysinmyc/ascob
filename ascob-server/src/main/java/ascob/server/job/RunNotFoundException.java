package ascob.server.job;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No such run")
public class RunNotFoundException extends Exception {

    public RunNotFoundException() {
        super();
    }

    public RunNotFoundException(Long runId) {
        super("Run identified by id "+runId+" not found");
    }

    public RunNotFoundException(String message) {
        super(message);
    }

}