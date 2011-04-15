package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Array;

public class ExprDotEvalArraySize implements ExprDotEval
{
    private static final Log log = LogFactory.getLog(ExprDotEvalArraySize.class);

    public Object evaluate(Object target, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        if (target == null) {
            return null;
        }
        return Array.getLength(target);
    }

    public Class getResultType()
    {
        return Integer.class;
    }
}
