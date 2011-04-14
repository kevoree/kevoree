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
import com.espertech.esper.collection.ArrayEventIterator;
import com.espertech.esper.collection.OneEventCollection;
import com.espertech.esper.core.StatementResultService;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprValidationException;
import com.espertech.esper.epl.spec.OnTriggerWindowUpdateDesc;
import com.espertech.esper.event.EventTypeSPI;
import com.espertech.esper.view.StatementStopService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

/**
 * View for the on-delete statement that handles removing events from a named window.
 */
public class NamedWindowOnUpdateView extends NamedWindowOnExprBaseView
{
    private static final Log log = LogFactory.getLog(NamedWindowOnUpdateView.class);
    private EventBean[] lastResult;
    private final StatementResultService statementResultService;
    private final EventTypeSPI eventTypeSPI;
    private final NamedWindowUpdateHelper updateHelper;

    /**
     * Ctor.
     * @param statementStopService for indicating a statement was stopped or destroyed for cleanup
     * @param lookupStrategy for handling trigger events to determine deleted events
     * @param removeStreamView to indicate which events to delete
     * @param statementResultService for coordinating on whether insert and remove stream events should be posted
     * @param exprEvaluatorContext context for expression evalauation
     * @param onTriggerDesc describes on-update
     * @throws com.espertech.esper.epl.expression.ExprValidationException when expression validation fails
     */
    public NamedWindowOnUpdateView(StatementStopService statementStopService,
                                 LookupStrategy lookupStrategy,
                                 NamedWindowRootView removeStreamView,
                                 StatementResultService statementResultService,
                                 ExprEvaluatorContext exprEvaluatorContext,
                                 OnTriggerWindowUpdateDesc onTriggerDesc)
            throws ExprValidationException
    {
        super(statementStopService, lookupStrategy, removeStreamView, exprEvaluatorContext);
        this.statementResultService = statementResultService;
        eventTypeSPI = (EventTypeSPI) removeStreamView.getEventType();
        updateHelper = NamedWindowUpdateHelper.make(eventTypeSPI, onTriggerDesc.getAssignments(), onTriggerDesc.getOptionalAsName());
    }

    public void handleMatching(EventBean[] triggerEvents, EventBean[] matchingEvents)
    {
        if ((matchingEvents == null) || (matchingEvents.length == 0)){
            return;
        }
        EventBean[] eventsPerStream = new EventBean[2];

        OneEventCollection newData = new OneEventCollection();
        OneEventCollection oldData = new OneEventCollection();

        for (EventBean triggerEvent : triggerEvents) {
            eventsPerStream[1] = triggerEvent;
            for (EventBean matchingEvent : matchingEvents) {
                EventBean copy = updateHelper.update(matchingEvent, eventsPerStream, super.getExprEvaluatorContext());
                newData.add(copy);
                oldData.add(matchingEvent);
            }
        }

        if (!newData.isEmpty())
        {
            // Events to delete are indicated via old data
            this.rootView.update(newData.toArray(), oldData.toArray());

            // The on-delete listeners receive the events deleted, but only if there is interest
            if (statementResultService.isMakeNatural() || statementResultService.isMakeSynthetic()) {
                updateChildren(newData.toArray(), oldData.toArray());
            }
        }

        // Keep the last delete records
        lastResult = matchingEvents;
    }

    public EventType getEventType()
    {
        return namedWindowEventType;
    }

    public Iterator<EventBean> iterator()
    {
        return new ArrayEventIterator(lastResult);
    }
}