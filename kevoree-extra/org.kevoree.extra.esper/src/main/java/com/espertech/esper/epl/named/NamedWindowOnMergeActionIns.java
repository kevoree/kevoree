package com.espertech.esper.epl.named;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.OneEventCollection;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

public class NamedWindowOnMergeActionIns extends NamedWindowOnMergeAction {
    private SelectExprProcessor insertHelper;

    public NamedWindowOnMergeActionIns(ExprEvaluator optionalFilter, SelectExprProcessor insertHelper) {
        super(optionalFilter);
        this.insertHelper = insertHelper;
    }

    public void apply(EventBean matchingEvent, EventBean[] eventsPerStream, OneEventCollection newData, OneEventCollection oldData, ExprEvaluatorContext exprEvaluatorContext) {
        EventBean event = insertHelper.process(eventsPerStream, true, true);
        newData.add(event);
    }
}
