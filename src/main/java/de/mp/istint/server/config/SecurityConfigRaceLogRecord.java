package de.mp.istint.server.config;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Profile("prod-racelog-recording")
@ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true")
@KeycloakConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfigRaceLogRecord extends KeycloakWebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        // @formatter:off
        http
                .authorizeRequests(a -> a.antMatchers("/", "/error").permitAll()
                        //TODO: Temporary for testing. Remove for production
                        .antMatchers("/hello").permitAll()
                        .antMatchers(HttpMethod.GET, "/raceevents/**").permitAll()
                        .antMatchers(HttpMethod.POST, "/raceevents/**").permitAll()
                        .antMatchers(HttpMethod.PUT, "/raceevents/**").permitAll()
                        .antMatchers(HttpMethod.DELETE, "/raceevents/**").denyAll()
                        //.antMatchers(HttpMethod.GET).permitAll()
                        .anyRequest().authenticated())
                .csrf(c -> c.disable())

                .cors() // note: this setting is not needed here since we use Spring MVC CORS (see Spring Security doc https://docs.spring.io/spring-security/site/docs/5.4.1/reference/html5/#cors)

        ;
        // @formatter:on
    }

    @Bean
    @ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true")
    public KeycloakSpringBootConfigResolver KeycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {

        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
        // return new NullAuthenticatedSessionStrategy();
    }

    @GetMapping("/error")
    @ResponseBody
    public String error(HttpServletRequest request) {
        String message = (String) request.getSession().getAttribute("error.message");
        request.getSession().removeAttribute("error.message");
        return message;
    }
}
