package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import net.sf.cglib.reflect.FastMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;

public class ExprDotMethodEvalNoDuck implements ExprDotEval
{
    private static final Log log = LogFactory.getLog(ExprDotMethodEvalNoDuck.class);

    private final FastMethod method;
    private final ExprEvaluator[] parameters;

    public ExprDotMethodEvalNoDuck(FastMethod method, ExprEvaluator[] parameters)
    {
        this.method = method;
        this.parameters = parameters;
    }

    public Object evaluate(Object target, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        if (target == null) {
            return null;
        }

		Object[] args = new Object[parameters.length];
		for(int i = 0; i < args.length; i++)
		{
			args[i] = parameters[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
		}

		try
		{
            return method.invoke(target, args);
		}
		catch (InvocationTargetException e)
		{
            String message = "Method '" + method.getName() +
                    "' of class '" + method.getDeclaringClass().getSimpleName() +
                    "' reported an exception: " +
                    e.getTargetException();
            log.error(message, e.getTargetException());
		}
        return null;
    }

    public Class getResultType()
    {
        return method.getReturnType();
    }
}
