/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.plan;

import com.espertech.esper.epl.join.exec.TableLookupStrategy;
import com.espertech.esper.epl.join.table.EventTable;
import com.espertech.esper.client.EventType;

/**
 * Abstract specification on how to perform a table lookup.
 */
public abstract class TableLookupPlan
{
    private int lookupStream;
    private int indexedStream;
    private int indexNum;

    /**
     * Instantiates the lookup plan into a execution strategy for the lookup.
     * @param indexesPerStream - tables for each stream
     * @param eventTypes - types of events in stream
     * @return lookup strategy instance
     */
    public abstract TableLookupStrategy makeStrategy(EventTable[][] indexesPerStream, EventType[] eventTypes);

    /**
     * Ctor.
     * @param lookupStream - stream number of stream that supplies event to be used to look up
     * @param indexedStream - - stream number of stream that is being access via index/table
     * @param indexNum - index to use for lookup
     */
    protected TableLookupPlan(int lookupStream, int indexedStream, int indexNum)
    {
        this.lookupStream = lookupStream;
        this.indexedStream = indexedStream;
        this.indexNum = indexNum;
    }

    /**
     * Returns the lookup stream.
     * @return lookup stream
     */
    public int getLookupStream()
    {
        return lookupStream;
    }

    /**
     * Returns indexed stream.
     * @return indexed stream
     */
    public int getIndexedStream()
    {
        return indexedStream;
    }

    /**
     * Returns index number to use for looking up in.
     * @return index number
     */
    public int getIndexNum()
    {
        return indexNum;
    }

    public String toString()
    {
        return "lookupStream=" + lookupStream +
               " indexedStream=" + indexedStream +
               " indexNum=" + indexNum;
    }
}
