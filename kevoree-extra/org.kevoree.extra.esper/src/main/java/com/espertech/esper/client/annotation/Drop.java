package com.espertech.esper.client.annotation;

/**
 * An execution directive for use in an EPL statement, that causes processing of an event to stop after the EPL statement
 * marked with @Drop has processed the event, applicable only if multiple statements must process the same event.
 * <p>
 * Ensure the engine configuration for prioritized execution is set before using this annotation.
 */
public @interface Drop
{
}
