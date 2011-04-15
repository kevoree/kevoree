/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.named;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Determine events to be deleted from a named window using the where-clause and full table scan.
 */
public class LookupStrategyTableScan implements LookupStrategy
{
    private final ExprEvaluator joinExpr;
    private final EventBean[] eventsPerStream;
    private final Iterable<EventBean> iterableNamedWindow;

    /**
     * Ctor.
     * @param joinExpr is the where clause
     * @param iterable is the named window's data window iterator
     */
    public LookupStrategyTableScan(ExprEvaluator joinExpr, Iterable<EventBean> iterable)
    {
        this.joinExpr = joinExpr;
        this.eventsPerStream = new EventBean[2];
        this.iterableNamedWindow = iterable;
    }

    public EventBean[] lookup(EventBean[] newData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Set<EventBean> removeEvents = null;

        Iterator<EventBean> eventsIt = iterableNamedWindow.iterator();
        for (;eventsIt.hasNext();)
        {
            eventsPerStream[0] = eventsIt.next();   // next named window event

            for (EventBean aNewData : newData)
            {
                eventsPerStream[1] = aNewData;    // Stream 1 events are the originating events (on-delete events)

                Boolean result = (Boolean) joinExpr.evaluate(eventsPerStream, true, exprEvaluatorContext);
                if (result != null)
                {
                    if (result)
                    {
                        if (removeEvents == null)
                        {
                            removeEvents = new LinkedHashSet<EventBean>();
                        }
                        removeEvents.add(eventsPerStream[0]);
                    }
                }
            }
        }

        if (removeEvents == null)
        {
            return null;
        }

        return removeEvents.toArray(new EventBean[removeEvents.size()]);
    }
}
