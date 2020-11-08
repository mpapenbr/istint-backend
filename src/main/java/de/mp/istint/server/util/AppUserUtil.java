package de.mp.istint.server.util;

import java.util.Optional;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import de.mp.istint.server.security.UserPrincipal;

@Component
public class AppUserUtil {

    public Optional<UserPrincipal> getCurrentUserOld() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(auth)
                .map(a -> a.getPrincipal())
                .filter(item -> item instanceof UserPrincipal)
                .map(item -> (UserPrincipal) item);

    }

    public AccessToken getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        KeycloakAuthenticationToken keycloakAuthenticationToken = (KeycloakAuthenticationToken) auth;
        AccessToken accessToken = keycloakAuthenticationToken.getAccount().getKeycloakSecurityContext().getToken();
        return accessToken;

    }
}