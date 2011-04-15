/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.EventBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.espertech.esper.timer.TimeSourceService;

/**
 * A spin-locking implementation of a latch for use in guaranteeing delivery between
 * a single event produced by a single statement and consumable by another statement.
 */
public class InsertIntoLatchSpin
{
    private static final Log log = LogFactory.getLog(InsertIntoLatchSpin.class);

    // The earlier latch is the latch generated before this latch
    private InsertIntoLatchSpin earlier;
    private long msecTimeout;
    private EventBean payload;
    private TimeSourceService timeSourceService;

    private volatile boolean isCompleted;

    /**
     * Ctor.
     * @param earlier the latch before this latch that this latch should be waiting for
     * @param msecTimeout the timeout after which delivery occurs
     * @param payload the payload is an event to deliver
     * @param timeSourceService time source provider
     */
    public InsertIntoLatchSpin(InsertIntoLatchSpin earlier, long msecTimeout, EventBean payload, TimeSourceService timeSourceService)
    {
        this.earlier = earlier;
        this.msecTimeout = msecTimeout;
        this.payload = payload;
        this.timeSourceService = timeSourceService;
    }

    /**
     * Ctor - use for the first and unused latch to indicate completion.
     * @param timeSourceService time source provider
     */
    public InsertIntoLatchSpin(TimeSourceService timeSourceService)
    {
        isCompleted = true;
        earlier = null;
        msecTimeout = 0;
    }

    /**
     * Returns true if the dispatch completed for this future.
     * @return true for completed, false if not
     */
    public boolean isCompleted()
    {
        return isCompleted;
    }

    /**
     * Blocking call that returns only when the earlier latch completed.
     * @return payload of the latch
     */
    public EventBean await()
    {
        if (!earlier.isCompleted)
        {
            long spinStartTime = timeSourceService.getTimeMillis();

            while(!earlier.isCompleted)
            {
                Thread.yield();

                long spinDelta = timeSourceService.getTimeMillis() - spinStartTime;
                if (spinDelta > msecTimeout)
                {
                    log.info("Spin wait timeout exceeded in insert-into dispatch at " + msecTimeout + "ms, consider disabling insert-into between-statement latching for better performance");
                    break;
                }
            }
        }

        return payload;
    }

    /**
     * Called to indicate that the latch completed and a later latch can start.
     */
    public void done()
    {
        isCompleted = true;
        earlier = null;
    }
}
