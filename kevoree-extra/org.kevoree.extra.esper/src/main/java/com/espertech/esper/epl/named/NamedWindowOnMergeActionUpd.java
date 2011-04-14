package com.espertech.esper.epl.named;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.OneEventCollection;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

public class NamedWindowOnMergeActionUpd extends NamedWindowOnMergeAction {
    private final NamedWindowUpdateHelper updateHelper;

    public NamedWindowOnMergeActionUpd(ExprEvaluator optionalFilter, NamedWindowUpdateHelper updateHelper) {
        super(optionalFilter);
        this.updateHelper = updateHelper;
    }

    public void apply(EventBean matchingEvent, EventBean[] eventsPerStream, OneEventCollection newData, OneEventCollection oldData, ExprEvaluatorContext exprEvaluatorContext) {
        EventBean copy = updateHelper.update(matchingEvent, eventsPerStream, exprEvaluatorContext);
        newData.add(copy);
        oldData.add(matchingEvent);
    }
}
