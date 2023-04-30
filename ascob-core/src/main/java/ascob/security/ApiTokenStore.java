package ascob.security;

public interface ApiTokenStore {

    String newToken(ApiTokenIdentity identity, long expirySeconds);

    void deleteTokenByIdentifier(String identifier);

    void cleanExpiredTokens();
}
