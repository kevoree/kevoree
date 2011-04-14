/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.util.MetaDefItem;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Specification for creating an event type/schema.
 */
public class CreateSchemaDesc implements MetaDefItem, Serializable
{
    private static final long serialVersionUID = 8400789369907593190L;
    
    private final String schemaName;
    private final Set<String> types;
    private final List<ColumnDesc> columns;
    private final Set<String> inherits;
    private final boolean variant;

    /**
     * Ctor.
     * @param schemaName name
     * @param types event type name(s)
     * @param columns column definition
     * @param inherits supertypes
     * @param variant variant streams
     */
    public CreateSchemaDesc(String schemaName, Set<String> types, List<ColumnDesc> columns, Set<String> inherits, boolean variant)
    {
        this.schemaName = schemaName;
        this.types = types;
        this.columns = columns;
        this.inherits = inherits;
        this.variant = variant;
    }

    /**
     * Returns schema name.
     * @return schema name
     */
    public String getSchemaName()
    {
        return schemaName;
    }

    /**
     * Returns column definitions.
     * @return column defs
     */
    public List<ColumnDesc> getColumns()
    {
        return columns;
    }

    /**
     * Returns supertypes.
     * @return supertypes
     */
    public Set<String> getInherits()
    {
        return inherits;
    }

    /**
     * Returns type name(s).
     * @return types
     */
    public Set<String> getTypes()
    {
        return types;
    }

    /**
     * Returns true for variant stream.
     * @return flag
     */
    public boolean isVariant()
    {
        return variant;
    }
}
