/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import com.espertech.esper.epl.spec.ColumnDesc;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Set;
import java.util.List;

/**
 * Represents a create-schema syntax for creating a new event type.
 */
public class CreateSchemaClause implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String schemaName;
    private Set<String> types;
    private List<SchemaColumnDesc> columns;
    private Set<String> inherits;
    private boolean variant;

    /**
     * Ctor.
     */
    public CreateSchemaClause() {
    }

    /**
     * Ctor.
     * @param schemaName name of type
     * @param types are for model-after, could be multiple when declaring a variant stream, or a single fully-qualified class name
     * @param variant true for variant stream
     */
    public CreateSchemaClause(String schemaName, Set<String> types, boolean variant)
    {
        this.schemaName = schemaName;
        this.types = types;
        this.variant = variant;
    }

    /**
     * Ctor.
     * @param schemaName name of type
     * @param columns column definition
     * @param inherits inherited types, if any
     */
    public CreateSchemaClause(String schemaName, List<SchemaColumnDesc> columns, Set<String> inherits)
    {
        this.schemaName = schemaName;
        this.columns = columns;
        this.inherits = inherits;
    }

    /**
     * Ctor.
     * @param schemaName name of type
     * @param types are for model-after, could be multiple when declaring a variant stream, or a single fully-qualified class name
     * @param variant true for variant stream
     * @param columns column definition
     * @param inherits inherited types, if any
     */
    public CreateSchemaClause(String schemaName, Set<String> types, List<SchemaColumnDesc> columns, Set<String> inherits, boolean variant)
    {
        this.schemaName = schemaName;
        this.types = types;
        this.columns = columns;
        this.inherits = inherits;
        this.variant = variant;
    }

    /**
     * Returns the type name, aka. schema name.
     * @return type name
     */
    public String getSchemaName()
    {
        return schemaName;
    }

    /**
     * Sets the type name.
     * @param schemaName to set
     */
    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    /**
     * Returns model-after types, i.e. (fully-qualified) class name or event type name(s), multiple for variant types.
     * @return type names or class names
     */
    public Set<String> getTypes()
    {
        return types;
    }

    /**
     * Sets model-after types, i.e. (fully-qualified) class name or event type name(s), multiple for variant types.
     * @param types type names or class names
     */
    public void setTypes(Set<String> types)
    {
        this.types = types;
    }

    /**
     * Returns the column definition.
     * @return column def
     */
    public List<SchemaColumnDesc> getColumns()
    {
        return columns;
    }

    /**
     * Sets the column definition.
     * @param columns column def
     */
    public void setColumns(List<SchemaColumnDesc> columns)
    {
        this.columns = columns;
    }

    /**
     * Returns the names of event types inherited from, if any
     * @return types inherited
     */
    public Set<String> getInherits()
    {
        return inherits;
    }

    /**
     * Sets the names of event types inherited from, if any
     * @param inherits types inherited
     */
    public void setInherits(Set<String> inherits)
    {
        this.inherits = inherits;
    }

    /**
     * Returns true for variant stream type, or false for regular event type.
     * @return indicator
     */
    public boolean isVariant()
    {
        return variant;
    }

    /**
     * Set true for variant stream type, or false for regular event type.
     * @param variant indicator
     */
    public void setVariant(boolean variant)
    {
        this.variant = variant;
    }

    /**
     * Render as EPL.
     * @param writer to output to
     */
    public void toEPL(StringWriter writer)
    {
        writer.append("create");
        if (variant) {
            writer.append(" variant");
        }
        writer.append(" schema ");
        writer.append(schemaName);
        writer.append(" as ");
        if ((types != null) && (!types.isEmpty())) {
            String delimiter = "";
            for (String type : types) {
                writer.append(delimiter);
                writer.append(type);
                delimiter = ", ";
            }
        }
        else {
            writer.append("(");
            String delimiter = "";
            for (SchemaColumnDesc col : columns) {
                writer.append(delimiter);
                col.toEPL(writer);
                delimiter = ", ";
            }
            writer.append(")");
        }

        if ((inherits != null) && (!inherits.isEmpty())) {
            writer.append(" inherits ");
            String delimiter = "";
            for (String name : inherits) {
                writer.append(delimiter);
                writer.append(name);
                delimiter = ", ";
            }
        }
    }
}
