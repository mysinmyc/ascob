package ascob.security;

import java.util.Set;

public interface ApiTokenValidator {
    ApiTokenIdentity validateToken(String token) throws InvalidTokenException;
}
