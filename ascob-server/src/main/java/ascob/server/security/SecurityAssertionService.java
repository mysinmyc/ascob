package ascob.server.security;

import ascob.security.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class SecurityAssertionService {

    public void assertAuthorized(Authentication authentication, Permission permission) throws NotAuthorizedException {
        if (!isAuthorized(authentication,permission)) {
            throw new NotAuthorizedException(permission);
        }
    }

    public boolean isAuthorized(Authentication authentication, Permission permission) {
        for (GrantedAuthority current : authentication.getAuthorities()) {
            if ( permission.name().equals(current.getAuthority())) {
                return true;
            }
        }
        return  false;
    }
}
