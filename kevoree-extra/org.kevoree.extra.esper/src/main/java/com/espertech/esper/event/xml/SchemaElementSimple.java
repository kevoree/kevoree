package com.espertech.esper.event.xml;

import java.io.Serializable;

/**
 * Represents a simple value in a schema.
 */
public class SchemaElementSimple implements SchemaElement, Serializable
{
    private static final long serialVersionUID = 7822657793312617963L;
    private final String name;
    private final String namespace;
    private final short xsSimpleType;     // Types from XSSimpleType
    private final String typeName;
    private final boolean isArray;
    private final Integer fractionDigits;

    /**
     * Ctor.
     * @param name name
     * @param namespace namespace
     * @param type is the simple element type
     * @param isArray if unbound
     * @param typeName name of type
     * @param fractionDigits fraction digits if defined
     */
    public SchemaElementSimple(String name, String namespace, short type, String typeName, boolean isArray, Integer fractionDigits)
    {
        this.name = name;
        this.namespace = namespace;
        this.xsSimpleType = type;
        this.isArray = isArray;
        this.typeName = typeName;
        this.fractionDigits = fractionDigits;
    }

    /**
     * Returns element name.
     * @return element name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns type.
     * @return type
     */
    public short getXsSimpleType()
    {
        return xsSimpleType;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public boolean isArray()
    {
        return isArray;
    }

    /**
     * Returns the type name.
     * @return type name
     */
    public String getTypeName()
    {
        return typeName;
    }

    /**
     * Returns the fraction digits if defined.
     * @return digits
     */
    public Integer getFractionDigits()
    {
        return fractionDigits;
    }

    public String toString()
    {
        return "Simple " + namespace + " " + name;
    }
}
