package ascob.server.security;

import ascob.security.Permission;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class NotAuthorizedException extends  Exception {

    Permission missingPermission;

    public NotAuthorizedException() {
        super();
    }
    public NotAuthorizedException(String message) {
        super(message);
    }

    public NotAuthorizedException(Permission missingPermission) {
        this("Missing  permission "+missingPermission.name());
        this.missingPermission = missingPermission;
    }
}
