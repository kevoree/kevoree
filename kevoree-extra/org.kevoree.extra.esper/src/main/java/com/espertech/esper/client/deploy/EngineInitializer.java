package com.espertech.esper.client.deploy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For use with server environments that support dynamic engine initialization (enterprise edition server),
 * indicates that this method should be called after the engine instance is initialized and the initial set of
 * EPL statements have been deployed,
 * for example to set up listeners and subscribers.
 * <p>
 * Apply this annotation to any method
 * that accepts a single string parameter providing the engine name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EngineInitializer {
}