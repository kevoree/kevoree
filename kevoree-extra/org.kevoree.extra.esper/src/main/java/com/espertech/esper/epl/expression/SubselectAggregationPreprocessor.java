package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.agg.AggregationService;

import java.util.Collection;
import java.util.Set;

public class SubselectAggregationPreprocessor {

    private final AggregationService aggregationService;
    private final ExprEvaluator filterExpr;

    public SubselectAggregationPreprocessor(AggregationService aggregationService, ExprEvaluator filterExpr) {
        this.aggregationService = aggregationService;
        this.filterExpr = filterExpr;
    }

    public void evaluate(EventBean[] eventsPerStream, Collection<EventBean> matchingEvents, ExprEvaluatorContext exprEvaluatorContext) {

        aggregationService.clearResults();

        if (matchingEvents == null) {
            return;
        }

        EventBean[] events = new EventBean[eventsPerStream.length + 1];
        System.arraycopy(eventsPerStream, 0, events, 1, eventsPerStream.length);

        for (EventBean subselectEvent : matchingEvents)
        {
            // Prepare filter expression event list
            events[0] = subselectEvent;

            Boolean pass = (Boolean) filterExpr.evaluate(events, true, exprEvaluatorContext);
            if ((pass != null) && (pass))
            {
                aggregationService.applyEnter(events, null, exprEvaluatorContext);
            }
        }                
    }
}
