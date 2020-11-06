package de.mp.istint.server.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

import de.mp.istint.server.model.User;

/**
 * These attributes will be used to create a {@link User} which is set into an otherwise empty
 * {@link SecurityContext}
 *
 * @author mpapenbr
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMyOwnCustomizedSecurityContextFactory.class)
public @interface WithMyOwnStuff {

    String username() default "whatever";

    String id() default "1234";

}