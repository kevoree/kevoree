package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.event.EventAdapterService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class EvalBaseFirstProp implements SelectExprProcessor {

    private static final Log log = LogFactory.getLog(EvalBaseFirstProp.class);

    private final SelectExprContext selectExprContext;
    private final EventType resultEventType;

    public EvalBaseFirstProp(SelectExprContext selectExprContext, EventType resultEventType) {
        this.selectExprContext = selectExprContext;
        this.resultEventType = resultEventType;
    }

    public abstract EventBean processFirstCol(Object result);

    public EventBean process(EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize)
    {
        ExprEvaluator[] expressionNodes = selectExprContext.getExpressionNodes();
        ExprEvaluatorContext exprEvaluatorContext = selectExprContext.getExprEvaluatorContext();

        Object first = expressionNodes[0].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        return processFirstCol(first);
    }

    public EventAdapterService getEventAdapterService() {
        return selectExprContext.getEventAdapterService();
    }

    public EventType getResultEventType()
    {
        return resultEventType;
    }
}