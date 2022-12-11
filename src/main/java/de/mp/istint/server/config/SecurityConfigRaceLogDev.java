package de.mp.istint.server.config;

import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import de.mp.istint.server.config.data.CorsData;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfigRaceLogDev {

    // @Bean
    // @Primary
    public AuthenticationManager authenticationManager(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = new KeycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
        return auth.build();

    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsData corsData) throws Exception {

        // @formatter:off
        http
                // .authenticationManager(authManager)
                .authorizeHttpRequests(a -> a.requestMatchers("/", "/error").permitAll()
                        //TODO: Temporary for testing. Remove for production
                        .requestMatchers("/hello").permitAll()

                        .requestMatchers("/raceevents/**").permitAll()
                        //.antMatchers(HttpMethod.GET).permitAll()
                        .anyRequest().authenticated())
                .csrf(c -> c.disable())
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .cors(c -> c.configurationSource(corsConfigurationSource(corsData)))

        ;
        // @formatter:on
        return http.build();

    }

    @Bean
    @ConfigurationProperties("istint.cors")
    public CorsData corsData() {
        return new CorsData();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsData corsData) {
        CorsConfiguration configuration = new CorsConfiguration();
        // configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedOrigins(corsData.getAllowedOrigins());
        configuration.setAllowedHeaders(corsData.getAllowedHeaders());
        configuration.setAllowedMethods(corsData.getAllowedMethods());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @GetMapping("/error")
    @ResponseBody
    public String error(HttpServletRequest request) {
        String message = (String) request.getSession().getAttribute("error.message");
        request.getSession().removeAttribute("error.message");
        return message;
    }
}
