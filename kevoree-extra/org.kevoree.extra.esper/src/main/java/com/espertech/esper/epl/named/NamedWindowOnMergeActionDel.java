package com.espertech.esper.epl.named;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.OneEventCollection;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

public class NamedWindowOnMergeActionDel extends NamedWindowOnMergeAction {

    public NamedWindowOnMergeActionDel(ExprEvaluator optionalFilter) {
        super(optionalFilter);
    }

    @Override
    public void apply(EventBean matchingEvent, EventBean[] eventsPerStream, OneEventCollection newData, OneEventCollection oldData, ExprEvaluatorContext exprEvaluatorContext) {
        oldData.add(matchingEvent);
    }
}
