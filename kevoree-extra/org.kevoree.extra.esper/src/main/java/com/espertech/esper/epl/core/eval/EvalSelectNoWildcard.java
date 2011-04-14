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

public class EvalSelectNoWildcard implements SelectExprProcessor {

    private static final Log log = LogFactory.getLog(EvalSelectNoWildcard.class);

    private final SelectExprContext selectExprContext;
    private final EventType resultEventType;

    public EvalSelectNoWildcard(SelectExprContext selectExprContext, EventType resultEventType) {
        this.selectExprContext = selectExprContext;
        this.resultEventType = resultEventType;
    }

    public EventBean process(EventBean[] eventsPerStream, boolean isNewData, boolean isSynthesize)
    {
        ExprEvaluator[] expressionNodes = selectExprContext.getExpressionNodes();
        String[] columnNames = selectExprContext.getColumnNames();
        ExprEvaluatorContext exprEvaluatorContext = selectExprContext.getExprEvaluatorContext();
        EventAdapterService eventAdapterService = selectExprContext.getEventAdapterService();

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

        return eventAdapterService.adaptorForTypedMap(props, resultEventType);
    }

    public EventType getResultEventType()
    {
        return resultEventType;
    }
}