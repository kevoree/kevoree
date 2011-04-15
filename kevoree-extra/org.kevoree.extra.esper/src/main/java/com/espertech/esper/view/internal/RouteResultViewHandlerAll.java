package com.espertech.esper.view.internal;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.core.InternalEventRouter;
import com.espertech.esper.core.EPStatementHandle;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.epl.core.ResultSetProcessor;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

/**
 * Handler for split-stream evaluating the all where-clauses and their matching select-clauses.
 */
public class RouteResultViewHandlerAll implements RouteResultViewHandler
{
    private final InternalEventRouter internalEventRouter;
    private final boolean[] isNamedWindowInsert;
    private final EPStatementHandle epStatementHandle;
    private final ResultSetProcessor[] processors;
    private final ExprEvaluator[] whereClauses;
    private final EventBean[] eventsPerStream = new EventBean[1];
    private final StatementContext statementContext;

    /**
     * Ctor.
     * @param epStatementHandle handle
     * @param internalEventRouter routes generated events
     * @param processors select clauses
     * @param whereClauses where clauses
     * @param statementContext statement services
     */
    public RouteResultViewHandlerAll(EPStatementHandle epStatementHandle, InternalEventRouter internalEventRouter, boolean[] isNamedWindowInsert, ResultSetProcessor[] processors, ExprEvaluator[] whereClauses, StatementContext statementContext)
    {
        this.internalEventRouter = internalEventRouter;
        this.isNamedWindowInsert = isNamedWindowInsert;
        this.epStatementHandle = epStatementHandle;
        this.processors = processors;
        this.whereClauses = whereClauses;
        this.statementContext = statementContext;
    }

    public boolean handle(EventBean event, ExprEvaluatorContext exprEvaluatorContext)
    {
        eventsPerStream[0] = event;
        boolean isHandled = false;

        for (int i = 0; i < whereClauses.length; i++)
        {
            Boolean pass = true;
            if (whereClauses[i] != null)
            {
                Boolean passEvent = (Boolean) whereClauses[i].evaluate(eventsPerStream, true, exprEvaluatorContext);
                if ((passEvent == null) || (!passEvent))
                {
                    pass = false;
                }
            }

            if (pass)
            {
                UniformPair<EventBean[]> result = processors[i].processViewResult(eventsPerStream, null, false);
                if ((result != null) && (result.getFirst() != null) && (result.getFirst().length > 0))
                {
                    isHandled = true;
                    internalEventRouter.route(result.getFirst()[0], epStatementHandle, statementContext.getInternalEventEngineRouteDest(), exprEvaluatorContext, isNamedWindowInsert[i]);
                }
            }
        }

        return isHandled;
    }
}
