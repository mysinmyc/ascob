package ascob.server.security;

import ascob.security.*;
import ascob.server.ApiInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@SecurityRequirements({@SecurityRequirement(name = ApiInfo.API_TOKEN_SCHEMA_NAME)})
@RestController
@RequestMapping("/api/security")
public class SecurityController {

    @Autowired
    SecurityAssertionService securityAssertionService;

    @Autowired
    ApiTokenValidator apiTokenValidator;

    @Operation(summary = "return the authentication info")
    @RequestMapping(value = "/whoami", method = RequestMethod.GET)
    public WhoAmIResponse whoami( Authentication authentication) {
        WhoAmIResponse whoAmIResponse = new WhoAmIResponse();
        whoAmIResponse.setAuthorities(
                authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
        );
        return whoAmIResponse;
    }

    @Operation(summary = "Generate a new api token")
    @RequestMapping(value = "/tokens/{identifier}",method = RequestMethod.POST)
    public CreateTokenResponse createToken(@Parameter(description = "token identifier") @PathVariable("identifier") String identifier,
                                           @RequestBody CreateTokenRequest request, Authentication authentication) throws NotAuthorizedException {
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

    @Operation(summary = "Revoke an api token")
    @RequestMapping(value = "/tokens/{identifier}",method = RequestMethod.DELETE)
    public void deleteTokenByIdentifier(@Parameter(description = "Token identifier") @PathVariable("identifier") String identifier, Authentication authentication) throws NotAuthorizedException {
        securityAssertionService.assertAuthorized(authentication, Permission.security_token_write);
        if (!  (apiTokenValidator instanceof ApiTokenStore)) {
            throw new RuntimeException("read only store");
        } else {
            ((ApiTokenStore)apiTokenValidator).deleteTokenByIdentifier(identifier);
        }
    }
}
