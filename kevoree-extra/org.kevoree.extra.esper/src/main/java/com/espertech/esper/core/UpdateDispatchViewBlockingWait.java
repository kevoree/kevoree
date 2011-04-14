/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.dispatch.DispatchService;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.view.ViewSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenience view for dispatching view updates received from a parent view to update listeners
 * via the dispatch service.
 */
public class UpdateDispatchViewBlockingWait extends UpdateDispatchViewBase
{
    private UpdateDispatchFutureWait currentFutureWait;
    private long msecTimeout;

    /**
     * Ctor.
     * @param dispatchService - for performing the dispatch
     * @param msecTimeout - timeout for preserving dispatch order through blocking
     * @param statementResultServiceImpl - handles result delivery
     */
    public UpdateDispatchViewBlockingWait(StatementResultService statementResultServiceImpl, DispatchService dispatchService, long msecTimeout)
    {
        super(statementResultServiceImpl, dispatchService);
        this.currentFutureWait = new UpdateDispatchFutureWait(); // use a completed future as a start
        this.msecTimeout = msecTimeout;
    }

    public void update(EventBean[] newData, EventBean[] oldData) {
        newResult(new UniformPair<EventBean[]>(newData, oldData));
    }

    public void newResult(UniformPair<EventBean[]> results)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            ViewSupport.dumpUpdateParams(".update for view " + this, results);
        }

        statementResultServiceImpl.indicate(results);

        if (!isDispatchWaiting.get())
        {
            UpdateDispatchFutureWait nextFutureWait;
            synchronized(this)
            {
                nextFutureWait = new UpdateDispatchFutureWait(this, currentFutureWait, msecTimeout);
                currentFutureWait.setLater(nextFutureWait);
                currentFutureWait = nextFutureWait;
            }
            dispatchService.addExternal(nextFutureWait);
            isDispatchWaiting.set(true);
        }
    }

    private static Log log = LogFactory.getLog(UpdateDispatchViewBlockingWait.class);
}
