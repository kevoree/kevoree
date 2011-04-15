/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.view;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.core.EPStatementHandle;
import com.espertech.esper.core.InternalEventRouter;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.core.UpdateDispatchView;
import com.espertech.esper.epl.spec.SelectClauseStreamSelectorEnum;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.event.NaturalEventBean;

/**
 * An output strategy that handles routing (insert-into) and stream selection.
 */
public class OutputStrategyPostProcess implements OutputStrategy
{
    private final boolean isRoute;
    private final boolean isRouteRStream;
    private final SelectClauseStreamSelectorEnum selectStreamDirEnum;
    private final InternalEventRouter internalEventRouter;
    private final EPStatementHandle epStatementHandle;
    private final StatementContext statementContext;
    private final boolean addToFront;

    /**
     * Ctor.
     * @param route true if this is insert-into
     * @param routeRStream true if routing the remove stream events, false if routing insert stream events
     * @param selectStreamDirEnum enumerator selecting what stream(s) are selected
     * @param internalEventRouter for performing the route operation
     * @param statementContext for statement-level services
     * @param epStatementHandle for use in routing to determine which statement routed
     */
    public OutputStrategyPostProcess(boolean route, boolean routeRStream, SelectClauseStreamSelectorEnum selectStreamDirEnum, InternalEventRouter internalEventRouter, EPStatementHandle epStatementHandle, StatementContext statementContext, boolean addToFront)
    {
        isRoute = route;
        isRouteRStream = routeRStream;
        this.selectStreamDirEnum = selectStreamDirEnum;
        this.internalEventRouter = internalEventRouter;
        this.epStatementHandle = epStatementHandle;
        this.statementContext = statementContext;
        this.addToFront = addToFront;
    }

    public void output(boolean forceUpdate, UniformPair<EventBean[]> result, UpdateDispatchView finalView)
    {
        EventBean[] newEvents = result != null ? result.getFirst() : null;
        EventBean[] oldEvents = result != null ? result.getSecond() : null;

        // route first
        if (isRoute)
        {
            if ((newEvents != null) && (!isRouteRStream))
            {
                route(newEvents, statementContext);
            }

            if ((oldEvents != null) && (isRouteRStream))
            {
                route(oldEvents, statementContext);
            }
        }

        // discard one side of results
        if (selectStreamDirEnum == SelectClauseStreamSelectorEnum.RSTREAM_ONLY)
        {
            newEvents = oldEvents;
            oldEvents = null;
        }
        else if (selectStreamDirEnum == SelectClauseStreamSelectorEnum.ISTREAM_ONLY)
        {
            oldEvents = null;   // since the insert-into may require rstream
        }

        // dispatch
        if(newEvents != null || oldEvents != null)
        {
            finalView.newResult(new UniformPair<EventBean[]>(newEvents, oldEvents));
        }
        else if(forceUpdate)
        {
            finalView.newResult(new UniformPair<EventBean[]>(null, null));
        }
    }

    private void route(EventBean[] events, ExprEvaluatorContext exprEvaluatorContext)
    {
        for (EventBean routed : events) {
            if (routed instanceof NaturalEventBean) {
                NaturalEventBean natural = (NaturalEventBean) routed;
                internalEventRouter.route(natural.getOptionalSynthetic(), epStatementHandle, statementContext.getInternalEventEngineRouteDest(), exprEvaluatorContext, addToFront);
            } else {
                internalEventRouter.route(routed, epStatementHandle, statementContext.getInternalEventEngineRouteDest(), exprEvaluatorContext, addToFront);
            }
        }
    }
}
