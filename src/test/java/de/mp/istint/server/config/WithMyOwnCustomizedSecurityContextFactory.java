package de.mp.istint.server.config;

import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import de.mp.istint.server.model.User;

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

public class WithMyOwnCustomizedSecurityContextFactory implements WithSecurityContextFactory<WithMyOwnUser> {

    @Override
    public SecurityContext createSecurityContext(WithMyOwnUser ownStuff) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User user = User.builder()
                .name(ownStuff.username())
                .id(ownStuff.id())
                .build();
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken(user, "password", authorities);
        context.setAuthentication(auth);
        return context;
    }

}