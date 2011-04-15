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
import java.util.*;

/**
 * Represents the in-clause (set check) function in an expression tree.
 */
public class ExprInNode extends ExprNode implements ExprEvaluator
{
    private final boolean isNotIn;

    private boolean mustCoerce;
    private boolean hasCollectionOrArray;

    private transient SimpleNumberCoercer coercer;
    private transient ExprEvaluator[] evaluators;

    private static final long serialVersionUID = -601723009914169907L;

    /**
     * Ctor.
     * @param isNotIn is true for "not in" and false for "in"
     */
    public ExprInNode(boolean isNotIn)
    {
        this.isNotIn = isNotIn;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }
    
    /**
     * Returns true for not-in, false for regular in
     * @return false for "val in (a,b,c)" or true for "val not in (a,b,c)"
     */
    public boolean isNotIn()
    {
        return isNotIn;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() < 2)
        {
            throw new ExprValidationException("The IN operator requires at least 2 child expressions");
        }
        evaluators = ExprNodeUtility.getEvaluators(this.getChildNodes());

        // Must be the same boxed type returned by expressions under this
        Class typeOne = JavaClassHelper.getBoxedType(evaluators[0].getType());

        // collections, array or map not supported
        if ((typeOne.isArray()) || (JavaClassHelper.isImplementsInterface(typeOne, Collection.class)) || (JavaClassHelper.isImplementsInterface(typeOne, Map.class)))
        {
            throw new ExprValidationException("Collection or array comparison is not allowed for the IN, ANY, SOME or ALL keywords");
        }

        List<Class> comparedTypes = new ArrayList<Class>();
        comparedTypes.add(typeOne);
        hasCollectionOrArray = false;
        for (int i = 0; i < this.getChildNodes().size() - 1; i++)
        {
            Class propType = evaluators[i + 1].getType();
            if (propType == null)
            {
                continue;
            }
            if (propType.isArray())
            {
                hasCollectionOrArray = true;
                if (propType.getComponentType() != Object.class)
                {
                    comparedTypes.add(propType.getComponentType());
                }
            }
            else if (JavaClassHelper.isImplementsInterface(propType, Collection.class))
            {
                hasCollectionOrArray = true;
            }
            else if (JavaClassHelper.isImplementsInterface(propType, Map.class))
            {
                hasCollectionOrArray = true;
            }
            else
            {
                comparedTypes.add(propType);
            }
        }

        // Determine common denominator type
        Class coercionType;
        try {
            coercionType = JavaClassHelper.getCommonCoercionType(comparedTypes.toArray(new Class[comparedTypes.size()]));
        }
        catch (CoercionException ex)
        {
            throw new ExprValidationException("Implicit conversion not allowed: " + ex.getMessage());
        }

        // Check if we need to coerce
        mustCoerce = false;
        if (JavaClassHelper.isNumeric(coercionType))
        {
            for (Class compareType : comparedTypes)
            {
                if (coercionType != JavaClassHelper.getBoxedType(compareType))
                {
                    mustCoerce = true;
                }
            }
            if (mustCoerce)
            {
                coercer = SimpleNumberCoercerFactory.getCoercer(null, JavaClassHelper.getBoxedType(coercionType));
            }
        }
    }

    public Class getType()
    {
        return Boolean.class;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object inPropResult = evaluators[0].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

        if (!hasCollectionOrArray)
        {
            if ((mustCoerce) && (inPropResult != null))
            {
                inPropResult = coercer.coerceBoxed((Number) inPropResult);
            }

            int len = this.getChildNodes().size() - 1;
            if ((len > 0) && (inPropResult == null))
            {
                return null;
            }
            boolean hasNullRow = false;
            for (int i = 1; i <= len; i++)
            {
                Object rightResult = evaluators[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

                if (rightResult == null)
                {
                    hasNullRow = true;
                    continue;
                }

                if (!mustCoerce)
                {
                    if (rightResult.equals(inPropResult))
                    {
                        return !isNotIn;
                    }
                }
                else
                {
                    Number right = coercer.coerceBoxed((Number) rightResult);
                    if (right.equals(inPropResult))
                    {
                        return !isNotIn;
                    }
                }
            }

            if (hasNullRow)
            {
                return null;
            }
            return isNotIn;
        }
        else
        {
            int len = this.getChildNodes().size() - 1;
            boolean hasNullRow = false;
            for (int i = 1; i <= len; i++)
            {
                Object rightResult = evaluators[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

                if (rightResult == null)
                {
                    continue;
                }
                if (rightResult instanceof Collection)
                {
                    if (inPropResult == null)
                    {
                        return null;
                    }
                    Collection coll = (Collection) rightResult;
                    if (coll.contains(inPropResult))
                    {
                        return !isNotIn;
                    }
                }
                else if (rightResult instanceof Map)
                {
                    if (inPropResult == null)
                    {
                        return null;
                    }
                    Map coll = (Map) rightResult;
                    if (coll.containsKey(inPropResult))
                    {
                        return !isNotIn;
                    }
                }
                else if (rightResult.getClass().isArray())
                {
                    int arrayLength = Array.getLength(rightResult);
                    if ((arrayLength > 0) && (inPropResult == null))
                    {
                        return null;
                    }
                    for (int index = 0; index < arrayLength; index++)
                    {
                        Object item = Array.get(rightResult, index);
                        if (item == null)
                        {
                            hasNullRow = true;
                            continue;
                        }
                        if (!mustCoerce)
                        {
                            if (inPropResult.equals(item))
                            {
                                return !isNotIn;
                            }
                        }
                        else
                        {
                            if (!(item instanceof Number))
                            {
                                continue;
                            }
                            Number left = coercer.coerceBoxed((Number) inPropResult);
                            Number right = coercer.coerceBoxed((Number) item);
                            if (left.equals(right))
                            {
                                return !isNotIn;
                            }
                        }
                    }
                }
                else
                {
                    if (inPropResult == null)
                    {
                        return null;
                    }
                    if (!mustCoerce)
                    {
                        if (inPropResult.equals(rightResult))
                        {
                            return !isNotIn;
                        }
                    }
                    else
                    {
                        Number left = coercer.coerceBoxed((Number) inPropResult);
                        Number right = coercer.coerceBoxed((Number) rightResult);
                        if (left.equals(right))
                        {
                            return !isNotIn;
                        }
                    }
                }
            }

            if (hasNullRow)
            {
                return null;
            }
            return isNotIn;
        }
    }

    public boolean isConstantResult()
    {
        return false;
    }        

    public boolean equalsNode(ExprNode node_)
    {
        if (!(node_ instanceof ExprInNode))
        {
            return false;
        }

        ExprInNode other = (ExprInNode) node_;
        return other.isNotIn == this.isNotIn;
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        String delimiter = "";

        Iterator<ExprNode> it = this.getChildNodes().iterator();
        buffer.append(it.next().toExpressionString());
        if (isNotIn)
        {
            buffer.append(" not in (");
        }
        else
        {
            buffer.append(" in (");
        }

        do
        {
            ExprNode inSetValueExpr = it.next();
            buffer.append(delimiter);
            buffer.append(inSetValueExpr.toExpressionString());
            delimiter = ",";
        }
        while (it.hasNext());

        buffer.append(')');
        return buffer.toString();
    }
}
