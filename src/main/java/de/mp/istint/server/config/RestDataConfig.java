package de.mp.istint.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import de.mp.istint.server.model.Event;
import de.mp.istint.server.model.User;

@Configuration
public class RestDataConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(Event.class, User.class);
    }
}