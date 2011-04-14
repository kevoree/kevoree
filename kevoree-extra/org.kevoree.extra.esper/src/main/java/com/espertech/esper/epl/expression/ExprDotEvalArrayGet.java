package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Array;

public class ExprDotEvalArrayGet implements ExprDotEval
{
    private static final Log log = LogFactory.getLog(ExprDotEvalArrayGet.class);

    private final Class componentType;
    private final ExprEvaluator indexExpression;

    public ExprDotEvalArrayGet(ExprEvaluator index, Class componentType)
    {
        this.indexExpression = index;
        this.componentType = componentType;
    }

    public Object evaluate(Object target, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        if (target == null) {
            return null;
        }

        Object index = indexExpression.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        if (index == null) {
            return null;
        }
        if (!(index instanceof Integer)) {
            return null;
        }
        int indexNum = (Integer) index;

        if (Array.getLength(target) <= indexNum) {
            return null;
        }
        return Array.get(target, indexNum);
    }

    public Class getResultType()
    {
        return componentType;
    }
}
