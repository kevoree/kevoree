package com.espertech.esper.client.annotation;

/**
 * Annotation for use in EPL statement to tag a statement with a name-value pair.
 */
public @interface Tag
{
    /**
     * Returns the tag name.
     * @return tag name.
     */
    public String name();

    /**
     * Returns the tag value.
     * @return tag value.
     */
    public String value();
}
