package ascob.server.security;

import ascob.server.TestClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("webtest")
public class SecurityAuthenticationTest {

    @Autowired
    TestClients testClients;

    @Test
    public void testUnprivilegedCredentials() {
        ResponseEntity<String> responseEntity = testClients.withoutPrivilegesToken().getForEntity("/api/security/whoami", String.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void testExpiredCredentials() {
        ResponseEntity<String> responseEntity = testClients.withExpiredToken().getForEntity("/api/security/whoami", String.class);
        assertEquals(403, responseEntity.getStatusCode().value());
    }


}
