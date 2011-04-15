package com.espertech.esper.client.annotation;

/**
 * Annotation for use in EPL statement to define a statement name.
 */
public @interface Name
{
    /**
     * Returns the statement name.
     * @return statement name
     */
    String value();
}
