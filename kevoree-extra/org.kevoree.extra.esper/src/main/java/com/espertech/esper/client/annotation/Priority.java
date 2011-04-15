package com.espertech.esper.client.annotation;

/**
 * An execution directive for use in an EPL statement, by which processing of an event by statements
 * start with the statement that has the highest priority, applicable only if multiple statements must process the same event.
 * <p>
 * Ensure the engine configuration for prioritized execution is set before using this annotation.
 * <p>
 * The default priority value is zero (0).
 */
public @interface Priority
{
    /**
     * Priority value.
     * @return value
     */
    public int value();
}
