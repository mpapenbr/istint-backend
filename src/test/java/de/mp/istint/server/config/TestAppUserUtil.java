package de.mp.istint.server.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import de.mp.istint.server.model.User;
import de.mp.istint.server.util.IAppUserUtil;

@Component
public class TestAppUserUtil implements IAppUserUtil {
    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Assert.isTrue(auth instanceof UsernamePasswordAuthenticationToken, "auth must be of type UserNamePasswordAuthenticationToken");
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) auth;
        if (token.getPrincipal() instanceof User) {

            return (User) token.getPrincipal();
        } else {
            return User.builder().name(token.getName()).id("InternalId").build();
        }
    }
}
