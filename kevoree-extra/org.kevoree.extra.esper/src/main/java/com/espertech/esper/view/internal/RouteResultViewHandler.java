package com.espertech.esper.view.internal;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

/**
 * Handler for incoming events for split-stream syntax, encapsulates where-clause evaluation strategies.
 */
public interface RouteResultViewHandler
{
    /**
     * Handle event.
     * @param event to handle
     * @param exprEvaluatorContext expression eval context
     * @return true if at least one match was found, false if not 
     */
    public boolean handle(EventBean event, ExprEvaluatorContext exprEvaluatorContext);
}
