package de.mp.istint.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

    // Klappt so noch nicht, muss noch gefixed werden!

    @ConfigurationProperties(prefix = "istint.app")
    @Bean
    public Redirects redirects() {
        System.out.println("CommonConfig.redirects()");
        return new Redirects();
    }

}
