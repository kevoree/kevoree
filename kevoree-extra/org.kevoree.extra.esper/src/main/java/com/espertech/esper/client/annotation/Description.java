package com.espertech.esper.client.annotation;

/**
 * Annotation for use in EPL statements to add a description.
 */
public @interface Description
{
    /**
     * Returns the description text.
     * @return description text
     */
    String value();
}
