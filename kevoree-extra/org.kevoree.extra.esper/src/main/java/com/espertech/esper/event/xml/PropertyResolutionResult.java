package com.espertech.esper.event.xml;

import com.espertech.esper.client.EventPropertyGetter;

/**
 * Result of a property resolution.
 */
public class PropertyResolutionResult
{
    private EventPropertyGetter getter;
    private Class returnType;
    private SchemaItem optionalSchemaItem;

    /**
     * Ctor.
     * @param getter getter for property values
     * @param returnType type of property
     * @param optionalSchemaItem schema item, or null if none provided (schema-less use)
     */
    public PropertyResolutionResult(EventPropertyGetter getter, Class returnType, SchemaItem optionalSchemaItem)
    {
        this.getter = getter;
        this.returnType = returnType;
        this.optionalSchemaItem = optionalSchemaItem;
    }

    /**
     * Returns the getter.
     * @return getter
     */
    public EventPropertyGetter getGetter()
    {
        return getter;
    }

    /**
     * Returns the type.
     * @return type
     */
    public Class getReturnType()
    {
        return returnType;
    }

    /**
     * Returns the schema item.
     * @return item
     */
    public SchemaItem getOptionalSchemaItem()
    {
        return optionalSchemaItem;
    }
}
