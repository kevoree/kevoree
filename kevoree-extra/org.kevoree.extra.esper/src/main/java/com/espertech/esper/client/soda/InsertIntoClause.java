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
 * An insert-into clause consists of a stream name and column names and an optional stream selector.
 */
public class InsertIntoClause implements Serializable
{
    private static final long serialVersionUID = 0L;

    private boolean insertStream;
    private String streamName;
    private List<String> columnNames;

    /**
     * Ctor.
     */
    public InsertIntoClause() {
    }

    /**
     * Creates the insert-into clause.
     * @param streamName the name of the stream to insert into
     * @return clause
     */
    public static InsertIntoClause create(String streamName)
    {
        return new InsertIntoClause(streamName);
    }

    /**
     * Creates the insert-into clause.
     * @param streamName the name of the stream to insert into
     * @param columns is a list of column names
     * @return clause
     */
    public static InsertIntoClause create(String streamName, String ...columns)
    {
        return new InsertIntoClause(streamName, columns);
    }

    /**
     * Creates the insert-into clause.
     * @param streamName the name of the stream to insert into
     * @param columns is a list of column names
     * @param streamSelector selects the stream
     * @return clause
     */
    public static InsertIntoClause create(String streamName, String[] columns, StreamSelector streamSelector)
    {
        if (streamSelector == StreamSelector.RSTREAM_ISTREAM_BOTH)
        {
            throw new IllegalArgumentException("Insert into only allows istream or rstream selection, not both");
        }
        return new InsertIntoClause(streamName, Arrays.asList(columns), streamSelector != StreamSelector.RSTREAM_ONLY);
    }

    /**
     * Ctor.
     * @param streamName is the stream name to insert into
     */
    public InsertIntoClause(String streamName)
    {
        this.insertStream = true;
        this.streamName = streamName;
        this.columnNames = new ArrayList<String>();
    }

    /**
     * Ctor.
     * @param streamName is the stream name to insert into
     * @param columnNames column names
     */
    public InsertIntoClause(String streamName, String[] columnNames)
    {
        this.insertStream = true;
        this.streamName = streamName;
        this.columnNames = Arrays.asList(columnNames);
    }

    /**
     * Ctor.
     * @param streamName is the stream name to insert into
     * @param columnNames column names
     * @param isIStream is true for selecting the insert stream (default)
     */
    public InsertIntoClause(String streamName, List<String> columnNames, boolean isIStream)
    {
        this.insertStream = isIStream;
        this.streamName = streamName;
        this.columnNames = columnNames;
    }

    /**
     * Returns true for insert stream.
     * @return indicator insert stream
     */
    public boolean isInsertStream()
    {
        return insertStream;
    }

    /**
     * Set to true for insert stream.
     * @param insertStream indicator insert stream
     */
    public void setInsertStream(boolean insertStream)
    {
        this.insertStream = insertStream;
    }

    /**
     * Returns true if insert (new data) events are fed, or false for remove (old data) events are fed.
     * @return true for insert stream, false for remove stream
     */
    public boolean isIStream()
    {
        return insertStream;
    }

    /**
     * Returns true if insert (new data) events are fed, or false for remove (old data) events are fed.
     * @return true for insert stream, false for remove stream
     */
    public boolean getIStream()
    {
        return insertStream;
    }

    /**
     * Returns name of stream name to use for insert-into stream.
     * @return stream name
     */
    public String getStreamName()
    {
        return streamName;
    }

    /**
     * Returns a list of column names specified optionally in the insert-into clause, or empty if none specified.
     * @return column names or empty list if none supplied
     */
    public List<String> getColumnNames()
    {
        return columnNames;
    }

    /**
     * Set to true for insert stream.
     * @param IStream indicator insert stream
     */
    public void setIStream(boolean IStream)
    {
        insertStream = IStream;
    }

    /**
     * Set stream name.
     * @param streamName name
     */
    public void setStreamName(String streamName)
    {
        this.streamName = streamName;
    }

    /**
     * Add a column name to the insert-into clause.
     * @param columnName to add
     */
    public void add(String columnName)
    {
        columnNames.add(columnName);
    }

    /**
     * Set column names.
     * @param columnNames names
     */
    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * Renders the clause in textual representation.
     * @param writer to output to
     */
    public void toEPL(StringWriter writer)
    {
        writer.write("insert ");
        if (!insertStream)
        {
            writer.write("rstream ");
        }


        writer.write("into ");
        writer.write(streamName);

        if (columnNames.size() > 0)
        {
            writer.write("(");
            String delimiter = "";
            for (String name : columnNames)
            {
                writer.write(delimiter);
                writer.write(name);
                delimiter = ", ";
            }
            writer.write(")");
        }
        writer.write(' ');
    }
}
