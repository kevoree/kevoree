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
 * Table of events allowing add and remove. Lookup in table is coordinated
 * through the underlying implementation.
 */
public interface EventTable extends Iterable<EventBean>
{
    /**
     * Add events to table.
     * @param events to add
     */
    public void add(EventBean[] events);

    /**
     * Remove events from table.
     * @param events to remove
     */
    public void remove(EventBean[] events);

    /**
     * Returns an iterator over events in the table.
     * @return table iterator
     */
    public Iterator<EventBean> iterator();

    /**
     * Returns true if the index is empty, or false if not
     * @return true for empty index
     */
    public boolean isEmpty();

    /**
     * Clear out index.
     */
    public void clear();
}
