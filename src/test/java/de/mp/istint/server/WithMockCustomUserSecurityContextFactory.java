package de.mp.istint.server;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.util.Assert;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithUserDetails> {
    private MyTestUserDetailsService userDetailsService;

    public WithMockCustomUserSecurityContextFactory(MyTestUserDetailsService arg) {
        this.userDetailsService = arg;
    }

    @Override
    public SecurityContext createSecurityContext(WithUserDetails withUser) {

        String username = withUser.value();
        Assert.hasLength(username, "value() must be non-empty String");
        UserDetails principal = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }

}