/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.lookup;

import com.espertech.esper.epl.join.table.UnindexedEventTable;
import com.espertech.esper.client.EventBean;

import java.util.Set;

/**
 * Lookup on an unindexed table returning the full table as matching events.
 */
public class FullTableScanLookupStrategy implements TableLookupStrategy
{
    private UnindexedEventTable eventIndex;

    /**
     * Ctor.
     * @param eventIndex - table to use
     */
    public FullTableScanLookupStrategy(UnindexedEventTable eventIndex)
    {
        this.eventIndex = eventIndex;
    }

    public Set<EventBean> lookup(EventBean[] eventPerStream)
    {
        Set<EventBean> result = eventIndex.getEventSet();
        if (result.isEmpty())
        {
            return null;
        }
        return result;
    }

    /**
     * Returns the associated table.
     * @return table for lookup.
     */
    public UnindexedEventTable getEventIndex()
    {
        return eventIndex;
    }
}
