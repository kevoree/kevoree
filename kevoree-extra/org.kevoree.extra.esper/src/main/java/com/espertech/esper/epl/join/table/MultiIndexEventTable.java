/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.table;

import com.espertech.esper.client.EventBean;

import java.util.Iterator;

/**
 * An event table for holding multiple tables for use when multiple indexes of the same dataset must be entered into a cache
 * for use in historical data lookup.
 * <p>
 * Does not allow iteration, adding and removing events. Does allow clearing all tables and asking for
 * filled or empty tables. All tables are expected to be filled and empty at the same time,
 * reflecting multiple indexes on a single set of data.
 */
public class MultiIndexEventTable implements EventTable
{
    private final EventTable[] tables;

    /**
     * Ctor.
     * @param tables tables to hold
     */
    public MultiIndexEventTable(EventTable[] tables)
    {
        this.tables = tables;
    }

    /**
     * Returns all tables.
     * @return tables
     */
    public EventTable[] getTables()
    {
        return tables;
    }

    public void add(EventBean[] events)
    {
        throw new UnsupportedOperationException();
    }

    public void remove(EventBean[] events)
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<EventBean> iterator()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty()
    {
        return tables[0].isEmpty();
    }

    public void clear()
    {
        for (int i = 0; i < tables.length; i++)
        {
            tables[i].clear();
        }
    }
}
