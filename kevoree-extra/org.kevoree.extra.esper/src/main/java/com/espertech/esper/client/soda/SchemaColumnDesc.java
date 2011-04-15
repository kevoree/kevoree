package com.espertech.esper.client.soda;

import java.io.Serializable;
import java.io.StringWriter;

/**
 * Descriptor for use in create-schema syntax to define property name and type of an event property.
 */
public class SchemaColumnDesc implements Serializable
{
    private static final long serialVersionUID = 5068685531968720148L;

    private String name;
    private String type;
    private boolean array;

    /**
     * Ctor.
     */
    public SchemaColumnDesc() {
    }

    /**
     * Ctor.
     * @param name property name
     * @param type property type, can be any simple class name or fully-qualified class name or existing event type name
     * @param array true for array property
     */
    public SchemaColumnDesc(String name, String type, boolean array)
    {
        this.name = name;
        this.type = type;
        this.array = array;
    }

    /**
     * Returns property name.
     * @return name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns property type.
     * @return type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returns true for array properties.
     * @return indicator
     */
    public boolean isArray()
    {
        return array;
    }

    /**
     * Set property name.
     * @param name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Set property type.
     * @param type type
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Set array indicator.
     * @param array indicator
     */
    public void setArray(boolean array)
    {
        this.array = array;
    }

    /**
     * Render to EPL.
     * @param writer to render to
     */
    public void toEPL(StringWriter writer) {
        writer.write(name);
        writer.write(' ');
        writer.write(type);
        if (array) {
            writer.write("[]");
        }
    }

}
