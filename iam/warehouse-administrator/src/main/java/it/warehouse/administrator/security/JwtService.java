package it.warehouse.administrator.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class JwtService {

    @Inject
    JsonWebToken jwtToken;


    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";
    private static final String ADMIN_ROLE = "ADMIN";


    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    public boolean isAdmin() {
        return hasRole(ADMIN_ROLE);
    }

    public String getRawToken() {
        return jwtToken.getRawToken();
    }


    /**
     * Returns all realm-level roles from the {@code realm_access.roles} Keycloak claim.
     *
     * @return set of role names, empty set if the claim is absent or malformed
     */
    private Set<String> getRoles() {
        Map<String, Object> realmAccess = jwtToken.getClaim(REALM_ACCESS);
        if (realmAccess == null) {
            log.warn("getRoles: realm_access claim not found in token");
            return Set.of();
        }
        List<?> roles = (List<?>) realmAccess.get(ROLES);
        if (CollectionUtils.isEmpty(roles)) {
            log.warn("getRoles: roles list is empty or absent inside realm_access");
            return Set.of();
        }
        return roles.stream()
                .map(Object::toString)
                .map(s -> s.replace("\"", ""))
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }
}
