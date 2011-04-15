/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.util.JavaClassHelper;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.Method;
import java.util.List;

public class ExprDotNodeUtility
{
    public static ExprDotEval[] getChainEvaluators(Class inputType, List<ExprChainedSpec> chainSpec, MethodResolutionService methodResolutionService,
                                                    boolean isDuckTyping)
            throws ExprValidationException
    {
        ExprDotEval[] methodEvalsNoDuck = new ExprDotEval[chainSpec.size()];

        Class currentInputType = inputType;
        int count = 0;
        for (ExprChainedSpec chain : chainSpec) {

            ExprEvaluator[] paramEvals = new ExprEvaluator[chain.getParameters().size()];
            Class[] paramTypes = new Class[chain.getParameters().size()];
            for (int i = 0; i < chain.getParameters().size(); i++) {
                paramEvals[i] = chain.getParameters().get(i).getExprEvaluator();
                paramTypes[i] = paramEvals[i].getType();
            }

            if (currentInputType.isArray() &&
                    chain.getName().toLowerCase().equals("size") &&
                    paramTypes.length == 0 &&
                    count == chainSpec.size() - 1) {
                methodEvalsNoDuck[count] = new ExprDotEvalArraySize();
            }
            else if (currentInputType.isArray() &&
                    chain.getName().toLowerCase().equals("get") &&
                    paramTypes.length == 1 &&
                    JavaClassHelper.getBoxedType(paramTypes[0]) == Integer.class) {
                methodEvalsNoDuck[count] = new ExprDotEvalArrayGet(paramEvals[0], currentInputType.getComponentType());
            }
            else {
                // Try to resolve the method
                try
                {
                    Method method = methodResolutionService.resolveMethod(currentInputType, chain.getName(), paramTypes);
                    FastClass declaringClass = FastClass.create(Thread.currentThread().getContextClassLoader(), method.getDeclaringClass());
                    FastMethod fastMethod = declaringClass.getMethod(method);
                    methodEvalsNoDuck[count] = new ExprDotMethodEvalNoDuck(fastMethod, paramEvals);
                }
                catch(Exception e)
                {
                    if (!isDuckTyping) {
                        throw new ExprValidationException(e.getMessage());
                    }
                    else {
                        return null;
                    }
                }
            }
            currentInputType = methodEvalsNoDuck[count].getResultType();
            count++;
        }

        return methodEvalsNoDuck;
    }
}
