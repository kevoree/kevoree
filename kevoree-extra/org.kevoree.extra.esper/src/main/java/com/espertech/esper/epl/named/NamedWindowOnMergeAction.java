package com.espertech.esper.epl.named;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.OneEventCollection;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

public abstract class NamedWindowOnMergeAction {

    private final ExprEvaluator optionalFilter;

    protected NamedWindowOnMergeAction(ExprEvaluator optionalFilter) {
        this.optionalFilter = optionalFilter;
    }

    public boolean isApplies(EventBean[] eventsPerStream, ExprEvaluatorContext context) {
        if (optionalFilter == null) {
            return true;
        }
        Object result = optionalFilter.evaluate(eventsPerStream, true, context);
        return result != null && (Boolean) result;
    }

    public abstract void apply(EventBean matchingEvent, EventBean[] eventsPerStream, OneEventCollection newData, OneEventCollection oldData, ExprEvaluatorContext exprEvaluatorContext);
}
