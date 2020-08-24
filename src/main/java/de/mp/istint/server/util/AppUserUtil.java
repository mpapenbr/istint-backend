package de.mp.istint.server.util;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import de.mp.istint.server.security.UserPrincipal;

@Component
public class AppUserUtil {

    public Optional<UserPrincipal> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(auth)
                .map(a -> a.getPrincipal())
                .filter(item -> item instanceof UserPrincipal)
                .map(item -> (UserPrincipal) item);

    }
}