package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprInsertEventBean;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

public class EvalInsertNative implements SelectExprProcessor {

    private final EventType eventType;
    private final SelectExprInsertEventBean selectExprInsertEventBean;
    private final ExprEvaluatorContext exprEvaluatorContext;

    public EvalInsertNative(EventType eventType, SelectExprInsertEventBean selectExprInsertEventBean, ExprEvaluatorContext exprEvaluatorContext) {
        this.eventType = eventType;
        this.selectExprInsertEventBean = selectExprInsertEventBean;
        this.exprEvaluatorContext = exprEvaluatorContext;
    }

    public EventBean process(EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize) {
        return selectExprInsertEventBean.manufacture(eventsPerStream, isNewData, exprEvaluatorContext);
    }

    public EventType getResultEventType() {
        return eventType;
    }
}
