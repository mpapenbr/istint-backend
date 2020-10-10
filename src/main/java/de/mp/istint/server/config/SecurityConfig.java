package de.mp.istint.server.config;

import static org.springframework.security.config.Customizer.withDefaults;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsService customUserDetailsService;

    //@Bean
    // public WebClient rest(ClientRegistrationRepository clients, OAuth2AuthorizedClientRepository authz) {
    //     ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 = new ServletOAuth2AuthorizedClientExchangeFilterFunction(clients, authz);
    //     return WebClient.builder()
    //             .filter(oauth2).build();
    // }

    // @Bean
    // public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService(WebClient rest) {
    //     DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    //     return request -> {
    //         OAuth2User user = delegate.loadUser(request);
    //         System.out.println("SocialApplication.oauth2UserService() " + user);
    //         if (!"github".equals(request.getClientRegistration().getRegistrationId())) {
    //             return user;
    //         }
    //         OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(request.getClientRegistration(), user.getName(), request.getAccessToken());
    //         String url = user.getAttribute("organizations_url");
    //         List<Map<String, Object>> orgs = rest
    //                 .get().uri(url)
    //                 .attributes(oauth2AuthorizedClient(client))
    //                 .retrieve()
    //                 .bodyToMono(List.class)
    //                 .block();

    //         if (orgs.stream().anyMatch(org -> "spring-projects".equals(org.get("login")))) {
    //             return user;
    //         }

    //         throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Not in Spring Team", ""));
    //     };
    // }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        // auth.inMemoryAuthentication().passwordEncoder(encoder).withUser("spring")
        //         .password(encoder.encode("secret")).roles("USER");
        auth.userDetailsService(customUserDetailsService).passwordEncoder(encoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler("/");

        // @formatter:off
        http
                .authorizeRequests(a -> a.antMatchers("/", "/error").permitAll()
                        .anyRequest().authenticated())
                .csrf(c -> c.disable())
                .httpBasic(b -> {
                })

        //        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        //         .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        //        .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
        //        .logout(l -> l.logoutSuccessUrl("/").permitAll())
        // .oauth2Login(o -> o.failureHandler((request, response, exception) -> {
        //     request.getSession().setAttribute("error.message", exception.getMessage());
        //     handler.onAuthenticationFailure(request, response, exception);
        // }));
        ;
        // @formatter:on
    }

    protected void configureY(HttpSecurity http) throws Exception {
        http
                .authorizeRequests((authorizeRequests) -> authorizeRequests
                        .mvcMatchers("/", "/public/**", "/error", "/oauth2/**", "/login/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(withDefaults())
                .oauth2Login(withDefaults())
                .oauth2Client(withDefaults());
    }

    //@Override
    protected void configureX(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .csrf().disable()
                .cors()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                .authorizeRequests()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/login/*").permitAll()
                .antMatchers("/callback/*").permitAll()
                .antMatchers("/auth/**", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .and()
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults());
        // @formatter:on
    }

    @GetMapping("/error")
    @ResponseBody
    public String error(HttpServletRequest request) {
        String message = (String) request.getSession().getAttribute("error.message");
        request.getSession().removeAttribute("error.message");
        return message;
    }
}
