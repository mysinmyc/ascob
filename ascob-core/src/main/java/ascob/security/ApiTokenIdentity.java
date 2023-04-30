package ascob.security;

import java.util.Collection;
import java.util.Set;

public class ApiTokenIdentity {


    public ApiTokenIdentity() {
    }

    public ApiTokenIdentity(String identifier) {
        this.identifier=identifier;
    }

    public ApiTokenIdentity(String identifier, Collection<Permission> permissions) {
        this.identifier=identifier;
        this.permissions = permissions;
    }

    String identifier;

    Collection<Permission> permissions;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<Permission> permissions) {
        this.permissions = permissions;
    }

}
