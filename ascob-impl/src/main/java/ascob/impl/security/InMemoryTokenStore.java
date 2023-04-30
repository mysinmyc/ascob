package ascob.impl.security;

import ascob.security.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@ConditionalOnProperty(matchIfMissing = true, name= "security.tokenStore.inmemory.enabled", havingValue = "true")
@Component
public class InMemoryTokenStore implements ApiTokenStore, ApiTokenValidator, InitializingBean {

    @Value("${security.rootToken:}")
    String rootToken;

    @Value("${security.tokens.webhook:}")
    List<String> webhookTokens;

    @Value("${security.tokens.jobManager:}")
    List<String> jobTokens;

    private boolean addFixedToken(String token, Collection<Permission> permissions) {
        if (token==null || token.isEmpty()) {
            return false;
        }
        InternalToken internalToken = new InternalToken();
        ApiTokenIdentity identity = new ApiTokenIdentity();
        identity.setPermissions(permissions);
        internalToken.identity = identity;
        tokens.put(token,internalToken);
        return true;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        addFixedToken(rootToken,Set.of( Permission.values()));
        webhookTokens.forEach(t->addFixedToken(t, Arrays.stream(Permission.values()).filter( p -> p.getGroup().equals("webhook")).toList()));
        jobTokens.forEach(t->addFixedToken(t, Arrays.stream(Permission.values()).filter( p -> p.getGroup().equals("job")).toList()));
    }

    static class InternalToken {
        ApiTokenIdentity identity;
        LocalDateTime expiry;
        String token;

        boolean isExpired() {
            return expiry!=null && LocalDateTime.now().isAfter(expiry);
        }
    }
    Map<String, InternalToken> tokens = new ConcurrentHashMap<>();
    Map<String,String> identifier2Tokens = new ConcurrentHashMap<>();

    @Override
    public String newToken(ApiTokenIdentity identity, long expirySeconds) {
        InternalToken token = new InternalToken();
        token.identity = identity;
        token.token = UUID.randomUUID().toString();
        if (expirySeconds!=0) {
            token.expiry = LocalDateTime.now().plusSeconds(expirySeconds);
        }
        tokens.put(token.token, token);
        identifier2Tokens.put(identity.getIdentifier(),token.token);
        return token.token;
    }

    @Override
    public void deleteTokenByIdentifier(String identifier) {
        String token = identifier2Tokens.get(identifier);
        if (token!=null) {
            tokens.remove(token);
            identifier2Tokens.remove(identifier);
        }
    }

    @Override
    public ApiTokenIdentity validateToken(String token) throws InvalidTokenException {
        if(token==null|| token.isEmpty()) {
            throw new InvalidTokenException();
        }
        InternalToken internalToken = tokens.get(token);
        if (internalToken==null) {
            throw new InvalidTokenException();
        }
        if (internalToken.isExpired()) {
            tokens.remove(token);
            identifier2Tokens.remove(internalToken.identity.getIdentifier());
            throw new InvalidTokenException();
        }
        return internalToken.identity;
    }


    @Value("${security.tokenStore.cleaner.maxBatchSize:1000}")
    int cleanerMaxBatchSize;


    @Async
    @Scheduled(initialDelayString="${security.tokenStore.cleaner.delay_ms:180000}",  fixedRateString = "${security.tokenStore.cleaner:180000}")
    public void cleanExpiredTokens() {

        List<InternalToken> tokensToDelete = new ArrayList<>();
        for (Map.Entry<String,InternalToken> tokenEntry: tokens.entrySet()) {
            if (tokenEntry.getValue().isExpired()) {
                tokensToDelete.add(tokenEntry.getValue());
            }
            if (tokensToDelete.size() >= cleanerMaxBatchSize) {
                break;
            }
        }

        for ( InternalToken tokenToDelete : tokensToDelete) {
            tokens.remove(tokenToDelete.token);
            if (tokenToDelete.identity.getIdentifier()!=null && ! tokenToDelete.identity.getIdentifier().isEmpty()) {
                identifier2Tokens.remove(tokenToDelete.identity.getIdentifier());
            }
        }
    }

    public int size() {
        return tokens.size();
    }

    public void clear() {
        tokens.clear();
        identifier2Tokens.clear();
    }
}
