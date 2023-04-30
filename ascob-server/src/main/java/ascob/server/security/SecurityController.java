package ascob.server.security;

import ascob.security.CreateTokenRequest;
import ascob.security.CreateTokenResponse;
import ascob.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    @Autowired
    SecurityAssertionService securityAssertionService;

    @Autowired
    ApiTokenValidator apiTokenValidator;

    @RequestMapping(value = "/whoami")
    public String whoami( Authentication authentication) {
        return authentication.getName();
    }

    @RequestMapping(value = "/tokens/{identifier}",method = RequestMethod.POST)
    public CreateTokenResponse createToken(@PathVariable("identifier") String identifier, @RequestBody CreateTokenRequest request, Authentication authentication) throws NotAuthorizedException {
        securityAssertionService.assertAuthorized(authentication,Permission.security_token_write);
        if (! (apiTokenValidator instanceof ApiTokenStore)) {
            throw new RuntimeException("read only store");
        } else {
            ApiTokenIdentity identity = new ApiTokenIdentity();
            identity.setIdentifier(identifier);
            identity.setPermissions(request.getPermissions());
            String secret = ((ApiTokenStore)apiTokenValidator).newToken(identity, request.getExpirySeconds());
            CreateTokenResponse response = new CreateTokenResponse();
            response.setSecret(secret);
            return response;
        }
    }

    @RequestMapping(value = "/tokens/{identifier}",method = RequestMethod.DELETE)
    public void deleteTokenByIdentifier(@PathVariable("identifier") String identifier, Authentication authentication) throws NotAuthorizedException {
        securityAssertionService.assertAuthorized(authentication, Permission.security_token_write);
        if (!  (apiTokenValidator instanceof ApiTokenStore)) {
            throw new RuntimeException("read only store");
        } else {
            ((ApiTokenStore)apiTokenValidator).deleteTokenByIdentifier(identifier);
        }
    }
}
