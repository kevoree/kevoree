package com.espertech.esper.epl.core.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.SelectExprProcessor;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.event.EventAdapterService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class EvalBase implements SelectExprProcessor {

    private static final Log log = LogFactory.getLog(EvalBase.class);

    private final SelectExprContext selectExprContext;
    private final EventType resultEventType;

    public EvalBase(SelectExprContext selectExprContext, EventType resultEventType) {
        this.selectExprContext = selectExprContext;
        this.resultEventType = resultEventType;
    }

    public abstract EventBean processSpecific(Map<String, Object> props, EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize);

    public EventBean process(EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize)
    {
        ExprEvaluator[] expressionNodes = selectExprContext.getExpressionNodes();
        String[] columnNames = selectExprContext.getColumnNames();
        ExprEvaluatorContext exprEvaluatorContext = selectExprContext.getExprEvaluatorContext();

        // Evaluate all expressions and build a map of name-value pairs
        Map<String, Object> props;
        if (expressionNodes.length == 0)
        {
            props = Collections.EMPTY_MAP;
        }
        else
        {
            props = new HashMap<String, Object>();
            for (int i = 0; i < expressionNodes.length; i++)
            {
                Object evalResult = expressionNodes[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                props.put(columnNames[i], evalResult);
            }
        }

        return processSpecific(props, eventsPerStream, isNewData, isSynthesize);
    }

    public EventAdapterService getEventAdapterService() {
        return selectExprContext.getEventAdapterService();
    }

    public EventType getResultEventType()
    {
        return resultEventType;
    }

    public String getFirstColumnName() {
        return selectExprContext.getColumnNames()[0];
    }

    public ExprEvaluator[] getExprNodes() {
        return selectExprContext.getExpressionNodes();
    }
}