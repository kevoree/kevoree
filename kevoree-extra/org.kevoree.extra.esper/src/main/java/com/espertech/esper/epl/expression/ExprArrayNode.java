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
import com.espertech.esper.util.CoercionException;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.SimpleNumberCoercer;
import com.espertech.esper.util.SimpleNumberCoercerFactory;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents an array in a filter expressiun tree.
 */
public class ExprArrayNode extends ExprNode implements ExprEvaluator
{
    private Class arrayReturnType;
    private boolean mustCoerce;
    private int length;

    private transient SimpleNumberCoercer coercer;
    private transient Object constantResult;
    private transient ExprEvaluator[] evaluators;
    
    private static final long serialVersionUID = 5533223915923867651L;

    /**
     * Ctor.
     */
    public ExprArrayNode()
    {
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        length = this.getChildNodes().size();
        evaluators = ExprNodeUtility.getEvaluators(this.getChildNodes());

        // Can be an empty array with no content
        if (this.getChildNodes().size() == 0)
        {
            arrayReturnType = Object.class;
            constantResult = new Object[0];
            return;
        }

        List<Class> comparedTypes = new LinkedList<Class>();
        for (int i = 0; i < length; i++)
        {
            comparedTypes.add(evaluators[i].getType());
        }

        // Determine common denominator type
        try {
            arrayReturnType = JavaClassHelper.getCommonCoercionType(comparedTypes.toArray(new Class[comparedTypes.size()]));

            // Determine if we need to coerce numbers when one type doesn't match any other type
            if (JavaClassHelper.isNumeric(arrayReturnType))
            {
                mustCoerce = false;
                for (Class comparedType : comparedTypes)
                {
                    if (comparedType != arrayReturnType)
                    {
                        mustCoerce = true;
                    }
                }
                if (mustCoerce)
                {
                    coercer = SimpleNumberCoercerFactory.getCoercer(null, arrayReturnType);
                }
            }
        }
        catch (CoercionException ex)
        {
            // expected, such as mixing String and int values, or Java classes (not boxed) and primitives
            // use Object[] in such cases
        }
        if (arrayReturnType == null)
        {
            arrayReturnType = Object.class;
        }

        // Determine if we are dealing with constants only
        Object[] results = new Object[length];
        int index = 0;
        for (ExprNode child : this.getChildNodes())
        {
            if (!child.isConstantResult())
            {
                results = null;  // not using a constant result
                break;
            }
            results[index] = evaluators[index].evaluate(null, false, exprEvaluatorContext);
            index++;
        }

        // Copy constants into array and coerce, if required
        if (results != null)
        {
            constantResult = Array.newInstance(arrayReturnType, length);
            for (int i = 0; i < length; i++)
            {
                if (mustCoerce)
                {
                    Number boxed = (Number) results[i];
                    if (boxed != null)
                    {
                        Object coercedResult = coercer.coerceBoxed(boxed);
                        Array.set(constantResult, i, coercedResult);
                    }
                }
                else
                {
                    Array.set(constantResult, i, results[i]);
                }
            }
        }
    }

    public boolean isConstantResult()
    {
        return constantResult != null;
    }

    public Class getType()
    {
        return Array.newInstance(arrayReturnType, 0).getClass();
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (constantResult != null)
        {
            return constantResult;
        }

        Object array = Array.newInstance(arrayReturnType, length);

        if (length == 0)
        {
            return array;
        }

        int index = 0;
        for (ExprEvaluator child : evaluators)
        {
            Object result = child.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            if (result != null)
            {
                if (mustCoerce)
                {
                    Number boxed = (Number) result;
                    Object coercedResult = coercer.coerceBoxed(boxed);
                    Array.set(array, index, coercedResult);
                }
                else
                {
                    Array.set(array, index, result);
                }
            }
            index++;
        }

        return array;
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        String delimiter = "";

        buffer.append("{");
        for (ExprNode expr : this.getChildNodes())
        {
            buffer.append(delimiter);
            buffer.append(expr.toExpressionString());
            delimiter = ",";
        }

        buffer.append('}');
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprArrayNode))
        {
            return false;
        }
        return true;
    }
}
