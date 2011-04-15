/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create an index on a named window.
 */
public class CreateIndexClause implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String indexName;
    private String windowName;
    private List<String> columns = new ArrayList<String>();

    /**
     * Ctor.
     */
    public CreateIndexClause() {
    }

    /**
     * Creates a clause to create a named window.
     * @param windowName is the name of the named window
     * @param properties properties to index
     * @param indexName name of index
     * @return create variable clause
     */
    public static CreateIndexClause create(String indexName, String windowName, String... properties)
    {
        return new CreateIndexClause(indexName, windowName, properties);
    }

    /**
     * Ctor.
     * @param windowName is the name of the window to create
     * @param indexName index name
     * @param properties properties to index
     */
    public CreateIndexClause(String indexName, String windowName, String[] properties)
    {
        this.indexName = indexName;
        this.windowName = windowName;
        columns.addAll(Arrays.asList(properties));
    }

    /**
     * Renders the clause in textual representation.
     * @param writer to output to
     */
    public void toEPL(StringWriter writer)
    {
        writer.write("create index ");
        writer.write(indexName);
        writer.write(" on ");
        writer.write(windowName);
        writer.write('(');
        String delimiter = "";
        for (String prop : columns)
        {
            writer.write(delimiter);
            writer.write(prop);
            delimiter = ", ";
        }
        writer.write(')');
    }

    /**
     * Returns index name.
     * @return name of index
     */
    public String getIndexName()
    {
        return indexName;
    }

    /**
     * Set index name.
     * @param indexName name of index
     */
    public void setIndexName(String indexName)
    {
        this.indexName = indexName;
    }

    /**
     * Returns window name.
     * @return name of window
     */
    public String getWindowName()
    {
        return windowName;
    }

    /**
     * Sets window.
     * @param windowName to index
     */
    public void setWindowName(String windowName)
    {
        this.windowName = windowName;
    }

    /**
     * Returns columns.
     * @return columns
     */
    public List<String> getColumns()
    {
        return columns;
    }

    /**
     * Sets columns.
     * @param columns to index
     */
    public void setColumns(List<String> columns)
    {
        this.columns = columns;
    }
}