package ascob.server.job;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Invalid job spec")
public class InvalidJobSpecException extends Exception {

    public InvalidJobSpecException() {
        super();
    }
    public InvalidJobSpecException(String s) {
        super(s);
    }
}
