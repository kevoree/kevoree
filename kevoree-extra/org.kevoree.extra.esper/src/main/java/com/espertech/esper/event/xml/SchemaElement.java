package com.espertech.esper.event.xml;

/**
 * Schema element is a simple or complex element.
 */
public interface SchemaElement extends SchemaItem
{
    /**
     * Returns the namespace.
     * @return namespace
     */
    public String getNamespace();

    /**
     * Returns the name.
     * @return name
     */
    public String getName();

    /**
     * Returns true for unbounded or max>1
     * @return array indicator
     */
    public boolean isArray();
}
