/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.ConfigurationEngineDefaults;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.timer.TimeSourceService;

/**
 * Class to hold a current latch per statement that uses an insert-into stream (per statement and insert-into stream
 * relationship).
 */
public class InsertIntoLatchFactory
{
    private final boolean useSpin;
    private final TimeSourceService timeSourceService;

    private InsertIntoLatchSpin currentLatchSpin;
    private InsertIntoLatchWait currentLatchWait;
    private long msecWait;

    /**
     * Ctor.
     * @param name the factory name
     * @param msecWait the number of milliseconds latches will await maximually
     * @param locking the blocking strategy to employ
     * @param timeSourceService time source provider
     */
    public InsertIntoLatchFactory(String name, long msecWait, ConfigurationEngineDefaults.Threading.Locking locking,
                                  TimeSourceService timeSourceService)
    {
        this.msecWait = msecWait;
        this.timeSourceService = timeSourceService;

        useSpin = (locking == ConfigurationEngineDefaults.Threading.Locking.SPIN);

        // construct a completed latch as an initial root latch
        if (useSpin)
        {
            currentLatchSpin = new InsertIntoLatchSpin(timeSourceService);
        }
        else
        {
            currentLatchWait = new InsertIntoLatchWait();
        }
    }

    /**
     * Returns a new latch.
     * <p>
     * Need not be synchronized as there is one per statement and execution is during statement lock.
     * @param payload is the object returned by the await.
     * @return latch
     */
    public Object newLatch(EventBean payload)
    {
        if (useSpin)
        {
            InsertIntoLatchSpin nextLatch = new InsertIntoLatchSpin(currentLatchSpin, msecWait, payload, timeSourceService);
            currentLatchSpin = nextLatch;
            return nextLatch;
        }
        else
        {
            InsertIntoLatchWait nextLatch = new InsertIntoLatchWait(currentLatchWait, msecWait, payload);
            currentLatchWait.setLater(nextLatch);
            currentLatchWait = nextLatch;
            return nextLatch;
        }
    }
}
