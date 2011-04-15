/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.named;

import com.espertech.esper.collection.ArrayEventIterator;
import com.espertech.esper.collection.MultiKey;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.core.EPStatementHandle;
import com.espertech.esper.core.InternalEventRouter;
import com.espertech.esper.core.StatementResultService;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.core.ResultSetProcessor;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.view.StatementStopService;
import com.espertech.esper.event.EventTypeSPI;
import com.espertech.esper.event.EventBeanReaderDefaultImpl;
import com.espertech.esper.event.EventBeanUtility;
import com.espertech.esper.event.EventBeanReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * View for the on-select statement that handles selecting events from a named window.
 */
public class NamedWindowOnSelectView extends NamedWindowOnExprBaseView
{
    private static final Log log = LogFactory.getLog(NamedWindowOnSelectView.class);

    private final InternalEventRouter internalEventRouter;
    private final boolean addToFront;
    private final ResultSetProcessor resultSetProcessor;
    private final EPStatementHandle statementHandle;
    private final StatementResultService statementResultService;
    private final StatementContext statementContext;
    private EventBean[] lastResult;
    private Set<MultiKey<EventBean>> oldEvents = new HashSet<MultiKey<EventBean>>();
    private boolean isDistinct;
    private EventBeanReader eventBeanReader;

    /**
     * Ctor.
     * @param statementStopService for indicating a statement was stopped or destroyed for cleanup
     * @param lookupStrategy for handling trigger events to determine deleted events
     * @param rootView the named window root view
     * @param internalEventRouter for insert-into behavior
     * @param resultSetProcessor for processing aggregation, having and ordering
     * @param statementHandle required for routing events
     * @param statementResultService for coordinating on whether insert and remove stream events should be posted
     * @param statementContext statement services
     * @param isDistinct is true for distinct output
     */
    public NamedWindowOnSelectView(StatementStopService statementStopService,
                                   LookupStrategy lookupStrategy,
                                   NamedWindowRootView rootView,
                                   InternalEventRouter internalEventRouter,
                                   boolean addToFront,
                                   ResultSetProcessor resultSetProcessor,
                                   EPStatementHandle statementHandle,
                                   StatementResultService statementResultService,
                                   StatementContext statementContext,
                                   boolean isDistinct)
    {
        super(statementStopService, lookupStrategy, rootView, statementContext);
        this.internalEventRouter = internalEventRouter;
        this.addToFront = addToFront;
        this.resultSetProcessor = resultSetProcessor;
        this.statementHandle = statementHandle;
        this.statementResultService = statementResultService;
        this.statementContext = statementContext;
        this.isDistinct = isDistinct;

        if (isDistinct)
        {
            if (resultSetProcessor.getResultEventType() instanceof EventTypeSPI)
            {
                eventBeanReader = ((EventTypeSPI) resultSetProcessor.getResultEventType()).getReader();
            }
            if (eventBeanReader == null)
            {
                eventBeanReader = new EventBeanReaderDefaultImpl(resultSetProcessor.getResultEventType());
            }
        }
    }

    public void handleMatching(EventBean[] triggerEvents, EventBean[] matchingEvents)
    {
        EventBean[] newData;

        // clear state from prior results
        resultSetProcessor.clear();

        // build join result
        // use linked hash set to retain order of join results for last/first/window to work most intuitively
        Set<MultiKey<EventBean>> newEvents = new LinkedHashSet<MultiKey<EventBean>>();
        for (int i = 0; i < triggerEvents.length; i++)
        {
            EventBean triggerEvent = triggerEvents[0];
            if (matchingEvents != null)
            {
                for (int j = 0; j < matchingEvents.length; j++)
                {
                    EventBean[] eventsPerStream = new EventBean[2];
                    eventsPerStream[0] = matchingEvents[j];
                    eventsPerStream[1] = triggerEvent;
                    newEvents.add(new MultiKey<EventBean>(eventsPerStream));
                }
            }
        }

        // process matches
        UniformPair<EventBean[]> pair = resultSetProcessor.processJoinResult(newEvents, oldEvents, false);
        newData = (pair != null ? pair.getFirst() : null);

        if (isDistinct)
        {
            newData = EventBeanUtility.getDistinctByProp(newData, eventBeanReader);
        }

        if (internalEventRouter != null)
        {
            if (newData != null)
            {
                for (int i = 0; i < newData.length; i++)
                {
                    internalEventRouter.route(newData[i], statementHandle, statementContext.getInternalEventEngineRouteDest(), statementContext, addToFront);
                }
            }
        }

        // The on-select listeners receive the events selected
        if ((newData != null) && (newData.length > 0))
        {
            // And post only if we have listeners/subscribers that need the data
            if (statementResultService.isMakeNatural() || statementResultService.isMakeSynthetic())
            {
                updateChildren(newData, null);
            }
        }
        lastResult = newData;
    }

    public EventType getEventType()
    {
        if (resultSetProcessor != null)
        {
            return resultSetProcessor.getResultEventType();
        }
        else
        {
            return namedWindowEventType;
        }
    }

    public Iterator<EventBean> iterator()
    {
        return new ArrayEventIterator(lastResult);
    }
}
