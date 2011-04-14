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
import com.espertech.esper.view.*;
import com.espertech.esper.collection.ArrayEventIterator;
import com.espertech.esper.core.StatementResultService;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

/**
 * View for the on-delete statement that handles removing events from a named window.
 */
public class NamedWindowOnDeleteView extends NamedWindowOnExprBaseView
{
    private static final Log log = LogFactory.getLog(NamedWindowOnDeleteView.class);
    private EventBean[] lastResult;
    private final StatementResultService statementResultService;

    /**
     * Ctor.
     * @param statementStopService for indicating a statement was stopped or destroyed for cleanup
     * @param lookupStrategy for handling trigger events to determine deleted events
     * @param removeStreamView to indicate which events to delete
     * @param statementResultService for coordinating on whether insert and remove stream events should be posted
     * @param exprEvaluatorContext context for expression evalauation
     */
    public NamedWindowOnDeleteView(StatementStopService statementStopService,
                                 LookupStrategy lookupStrategy,
                                 NamedWindowRootView removeStreamView,
                                 StatementResultService statementResultService,
                                 ExprEvaluatorContext exprEvaluatorContext)
    {
        super(statementStopService, lookupStrategy, removeStreamView, exprEvaluatorContext);
        this.statementResultService = statementResultService;
    }

    public void handleMatching(EventBean[] triggerEvents, EventBean[] matchingEvents)
    {
        if ((matchingEvents != null) && (matchingEvents.length > 0))
        {
            // Events to delete are indicated via old data
            this.rootView.update(null, matchingEvents);

            // The on-delete listeners receive the events deleted, but only if there is interest
            if (statementResultService.isMakeNatural() || statementResultService.isMakeSynthetic()) {
                updateChildren(matchingEvents, null);
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
