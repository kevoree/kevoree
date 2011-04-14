/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.join.table.EventTable;

import java.util.Iterator;

/**
 * Full table scan strategy for a poll-based cache result.
 */
public class HistoricalIndexLookupStrategyNoIndex implements HistoricalIndexLookupStrategy
{
    public Iterator<EventBean> lookup(EventBean lookupEvent, EventTable index)
    {
        return index.iterator();
    }
}
