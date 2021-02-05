package de.mp.istint.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import de.mp.istint.server.model.Event;
import de.mp.istint.server.model.User;
import de.mp.istint.server.model.racelog.RaceEvent;

/**
 * This configuration is used to configure what is included in auto-generated REST-responses (by
 * spring)
 */
@Configuration
public class RestDataConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        // we want Ids for Event and User
        config.exposeIdsFor(Event.class, User.class, RaceEvent.class);
    }
}