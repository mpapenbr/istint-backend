package de.mp.istint.server.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

import de.mp.istint.server.model.User;

/**
 * These attributes will be used to create a {@link User} which is set into an otherwise empty
 * {@link SecurityContext}
 *
 * @author mpapenbr
 *
 */

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = WithMyOwnCustomizedSecurityContextFactory.class)
public @interface WithMyOwnUser {

    String username() default "whatever";

    String id() default "1234";

    @AliasFor(annotation = WithSecurityContext.class)
    TestExecutionEvent setupBefore() default TestExecutionEvent.TEST_METHOD;

}