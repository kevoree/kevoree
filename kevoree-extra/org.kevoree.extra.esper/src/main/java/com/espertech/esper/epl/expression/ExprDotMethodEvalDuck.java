package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.MethodResolutionService;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ExprDotMethodEvalDuck implements ExprDotEval
{
    private static final Log log = LogFactory.getLog(ExprDotMethodEvalDuck.class);

    private final MethodResolutionService methodResolutionService;
    private final String methodName;
    private final Class[] parameterTypes;
    private final ExprEvaluator[] parameters;

    private Map<Class, FastMethod> cache;

    public ExprDotMethodEvalDuck(MethodResolutionService methodResolutionService, String methodName, Class[] parameterTypes, ExprEvaluator[] parameters)
    {
        this.methodResolutionService = methodResolutionService;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
        cache = new HashMap<Class, FastMethod>();
    }

    public Object evaluate(Object target, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        if (target == null) {
            return null;
        }

        FastMethod method;
        if (cache.containsKey(target.getClass())) {
            method = cache.get(target.getClass());
        }
        else {
            method = getFastMethod(target.getClass());
            cache.put(target.getClass(), method);
        }

        if (method == null) {
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

    private FastMethod getFastMethod(Class clazz)
    {
        try
        {
            Method method = methodResolutionService.resolveMethod(clazz, methodName, parameterTypes);
            FastClass declaringClass = FastClass.create(Thread.currentThread().getContextClassLoader(), method.getDeclaringClass());
            return declaringClass.getMethod(method);
        }
        catch(Exception e)
        {
            log.debug("Not resolved for class '" + clazz.getName() + "' method '" + methodName + "'");
        }
        return null;
    }

    public Class getResultType()
    {
        return Object.class;
    }
}
