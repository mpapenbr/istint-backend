package de.mp.istint.server.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import de.mp.istint.server.model.User;
import de.mp.istint.server.security.UserPrincipal;

/**
 * This is the most flexible way of getting a customized SecurityContext without interferences from
 * other mechanisms.
 * <p>
 * We may also use {@code @Autowired} here.
 *
 * @see {@link https://docs.spring.io/spring-security/site/docs/5.4.0/reference/html5/#test-method-withsecuritycontext}
 *
 * @author mpapenbr
 *
 */
public class WithMyOwnCustomizedSecurityContextFactory implements WithSecurityContextFactory<WithMyOwnStuff> {

    @Override
    public SecurityContext createSecurityContext(WithMyOwnStuff ownStuff) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UserPrincipal principal = UserPrincipal.create(User.builder()
                .name(ownStuff.username())
                .id(ownStuff.id())
                .build());
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "dummyPassword", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }

}