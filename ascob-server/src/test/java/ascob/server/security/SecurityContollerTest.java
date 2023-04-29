package ascob.server.security;

import ascob.security.*;
import ascob.server.TestClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientException;

import java.security.Permissions;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("webtest")
public class SecurityContollerTest {

    @Autowired
    TestClients testClients;
    @Test
    public void testManageToken(@Autowired ApiTokenValidator apiTokenValidator) {

        CreateTokenRequest request = new CreateTokenRequest();
        request.setExpirySeconds(0);
        request.setPermissions(List.of(Permission.job_run_output));


        assertThrows(RestClientException.class,()-> testClients.withoutPrivilegesToken().postForObject("/api/security/tokens/test", request, Object.class));

        CreateTokenResponse token= testClients.withSecurityManagerToken().postForObject("/api/security/tokens/test", request, CreateTokenResponse.class);
        assertDoesNotThrow( ()-> apiTokenValidator.validateToken(token.getSecret()));

        testClients.withSecurityManagerToken().delete("/api/security/tokens/test");

        assertThrows(InvalidTokenException.class, ()-> apiTokenValidator.validateToken(token.getSecret()));
    }
}
