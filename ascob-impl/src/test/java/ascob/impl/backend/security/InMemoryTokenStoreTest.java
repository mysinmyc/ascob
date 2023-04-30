package ascob.impl.backend.security;

import ascob.impl.security.InMemoryTokenStore;
import ascob.security.ApiTokenIdentity;
import ascob.security.InvalidTokenException;
import ascob.security.Permission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("testtokenstore")
public class InMemoryTokenStoreTest {


    @Autowired
    InMemoryTokenStore tokenStore;

    @Value("${security.rootToken:}")
    String rootToken;

    @Value("${security.tokens.webhook:}")
    List<String> webhookTokens;

    @Value("${security.tokens.jobManager:}")
    List<String> jobTokens;


    @Test
    public void testConfigTokens() throws Exception {
        tokenStore.clear();
        tokenStore.afterPropertiesSet();
        assertEquals(5, tokenStore.size());

        assertDoesNotThrow( ()->tokenStore.validateToken(rootToken));

        ApiTokenIdentity webhookIdentity = tokenStore.validateToken(webhookTokens.get(0));
        assertTrue( webhookIdentity.getPermissions().contains(Permission.webhook_identification_keys));

        ApiTokenIdentity jobManagerIdentity = tokenStore.validateToken(jobTokens.get(0));
        assertTrue( jobManagerIdentity.getPermissions().contains(Permission.job_submit));

    }

    @Test
    public void testToken() {

        tokenStore.clear();

        String secret= tokenStore.newToken(new ApiTokenIdentity("test"),0);

        assertEquals(1,tokenStore.size());

        assertDoesNotThrow( ()->tokenStore.validateToken(secret));

        tokenStore.deleteTokenByIdentifier("test");

        assertEquals(0,tokenStore.size());

        assertThrows(InvalidTokenException.class, ()-> tokenStore.validateToken(secret));

    }
    @Test
    public void testExpiry() {

        tokenStore.clear();

        tokenStore.newToken(new ApiTokenIdentity("prova"),0);

        tokenStore.newToken(new ApiTokenIdentity("expired1"),-1);
        tokenStore.newToken(new ApiTokenIdentity("expired2"),-2);

        assertEquals(3, tokenStore.size());

        tokenStore.cleanExpiredTokens();

        assertEquals(1, tokenStore.size());

    }


    @Configuration
    @ComponentScan(basePackageClasses = {InMemoryTokenStore.class})
    static class SpringConfigurationClass {

    }
}
