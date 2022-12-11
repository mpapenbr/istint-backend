package de.mp.istint.server.util;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import de.mp.istint.server.model.User;

// @Profile("keycloak")
@Component
public class AppUserUtil implements IAppUserUtil {

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof KeycloakAuthenticationToken) {
            KeycloakAuthenticationToken keycloakAuthenticationToken = (KeycloakAuthenticationToken) auth;
            AccessToken accessToken = keycloakAuthenticationToken.getAccount().getKeycloakSecurityContext().getToken();
            return User.builder()
                    .id(accessToken.getSubject())
                    .name(accessToken.getPreferredUsername())
                    .details(auth.getDetails())
                    .build();
        }
        if (auth instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwt = (JwtAuthenticationToken) auth;

            return User.builder()
                    .id(jwt.getToken().getSubject())
                    .name(jwt.getName())
                    .details(auth.getDetails())
                    .build();
        }
        if (auth instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken pwdToken = (UsernamePasswordAuthenticationToken) auth;

            return (User) pwdToken.getPrincipal();
        }

        return null;

    }
}