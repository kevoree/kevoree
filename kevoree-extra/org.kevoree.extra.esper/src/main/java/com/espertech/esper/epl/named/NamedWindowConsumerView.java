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
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.FlushedEventBuffer;
import com.espertech.esper.collection.OneEventCollection;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.property.PropertyEvaluator;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.view.StatementStopCallback;
import com.espertech.esper.view.StatementStopService;
import com.espertech.esper.view.ViewSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

/**
 * Represents a consumer of a named window that selects from a named window via a from-clause.
 * <p>
 * The view simply dispatches directly to child views, and keeps the last new event for iteration.
 */
public class NamedWindowConsumerView extends ViewSupport implements StatementStopCallback
{
    private static final Log log = LogFactory.getLog(NamedWindowConsumerView.class);
    private final ExprEvaluator[] filterList;
    private final EventType eventType;
    private final NamedWindowTailView tailView;
    private final ExprEvaluatorContext exprEvaluatorContext;
    private final PropertyEvaluator optPropertyEvaluator;
    private EventBean[] eventPerStream = new EventBean[1];
    private final FlushedEventBuffer optPropertyContainedBuffer;

    /**
     * Ctor.
     * @param eventType the event type of the named window
     * @param statementStopService for registering a callback when the view stopped, to unregister the statement as a consumer
     * @param tailView to indicate when the consumer stopped to remove the consumer
     * @param filterList is a list of filter expressions
     * @param exprEvaluatorContext context for expression evalauation
     */
    public NamedWindowConsumerView(ExprEvaluator[] filterList,
                                   PropertyEvaluator optPropertyEvaluator,
                                   EventType eventType,
                                   StatementStopService statementStopService,
                                   NamedWindowTailView tailView,
                                   ExprEvaluatorContext exprEvaluatorContext)
    {
        this.filterList = filterList;
        this.optPropertyEvaluator = optPropertyEvaluator;
        if (optPropertyEvaluator != null) {
            optPropertyContainedBuffer = new FlushedEventBuffer();
        }
        else {
            optPropertyContainedBuffer = null;
        }
        this.eventType = eventType;
        this.tailView = tailView;
        this.exprEvaluatorContext = exprEvaluatorContext;
        statementStopService.addSubscriber(this);
    }

    public void update(EventBean[] newData, EventBean[] oldData)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".update Received update, " +
                    "  newData.length==" + ((newData == null) ? 0 : newData.length) +
                    "  oldData.length==" + ((oldData == null) ? 0 : oldData.length));
        }

        // if we have a filter for the named window,
        if (filterList.length != 0)
        {
            newData = passFilter(newData, true, exprEvaluatorContext);
            oldData = passFilter(oldData, false, exprEvaluatorContext);
        }

        if (optPropertyEvaluator != null) {
            newData = getUnpacked(newData);
            oldData = getUnpacked(oldData);
        }

        if ((newData != null) || (oldData != null))
        {
            updateChildren(newData, oldData);
        }
    }

    private EventBean[] getUnpacked(EventBean[] data)
    {
        if (data == null) {
            return null;
        }
        if (data.length == 0) {
            return data;
        }

        for (int i = 0; i < data.length; i++) {
            EventBean[] unpacked = optPropertyEvaluator.getProperty(data[i], exprEvaluatorContext);
            optPropertyContainedBuffer.add(unpacked);
        }
        return optPropertyContainedBuffer.getAndFlush();
    }

    private EventBean[] passFilter(EventBean[] eventData, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        if ((eventData == null) || (eventData.length == 0))
        {
            return null;
        }

        OneEventCollection filtered = null;
        for (EventBean event : eventData)
        {
            eventPerStream[0] = event;
            boolean pass = true;
            for (ExprEvaluator filter : filterList)
            {
                Boolean result = (Boolean) filter.evaluate(eventPerStream, isNewData, exprEvaluatorContext);
                if ((result != null) && (!result))
                {
                    pass = false;
                    break;
                }
            }

            if (pass)
            {
                if (filtered == null)
                {
                    filtered = new OneEventCollection();
                }
                filtered.add(event);
            }
        }

        if (filtered == null)
        {
            return null;
        }
        return filtered.toArray();
    }

    public EventType getEventType()
    {
        if (optPropertyEvaluator != null) {
            return optPropertyEvaluator.getFragmentEventType();
        }
        return eventType;
    }

    public Iterator<EventBean> iterator()
    {
        return new FilteredEventIterator(filterList, tailView.iterator(), exprEvaluatorContext);
    }

    public void statementStopped()
    {
        tailView.removeConsumer(this);
    }
}
