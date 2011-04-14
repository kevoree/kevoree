/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.subquery;

import com.espertech.esper.collection.FlushedEventBuffer;
import com.espertech.esper.view.internal.BufferObserver;
import com.espertech.esper.epl.join.table.EventTable;

/**
 * Observer to a buffer that is filled by a subselect view when it posts events,
 * to be added and removed from indexes.
 */
public class SubselectBufferObserver implements BufferObserver
{
    private final EventTable eventIndex;

    /**
     * Ctor.
     * @param eventIndex index to update
     */
    public SubselectBufferObserver(EventTable eventIndex) {
        this.eventIndex = eventIndex;
    }

    public void newData(int streamId, FlushedEventBuffer newEventBuffer, FlushedEventBuffer oldEventBuffer)
    {
        eventIndex.add(newEventBuffer.getAndFlush());
        eventIndex.remove(oldEventBuffer.getAndFlush());
    }
}