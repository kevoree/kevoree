/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.type.MathArithTypeEnum;
import com.espertech.esper.util.JavaClassHelper;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Represents a simple Math (+/-/divide/*) in a filter expression tree.
 */
public class ExprMathNode extends ExprNode implements ExprEvaluator
{
    private final MathArithTypeEnum mathArithTypeEnum;
    private final boolean isIntegerDivision;
    private final boolean isDivisionByZeroReturnsNull;

    private transient MathArithTypeEnum.Computer arithTypeEnumComputer;
    private Class resultType;
    private transient ExprEvaluator evaluatorLeft;
    private transient ExprEvaluator evaluatorRight;
    private static final long serialVersionUID = 6479683588602862158L;

    /**
     * Ctor.
     * @param mathArithTypeEnum - type of math
     * @param isIntegerDivision - false for division returns double, true for using Java-standard integer division
     * @param isDivisionByZeroReturnsNull - false for division-by-zero returns infinity, true for null
     */
    public ExprMathNode(MathArithTypeEnum mathArithTypeEnum, boolean isIntegerDivision, boolean isDivisionByZeroReturnsNull)
    {
        this.mathArithTypeEnum = mathArithTypeEnum;
        this.isIntegerDivision = isIntegerDivision;
        this.isDivisionByZeroReturnsNull = isDivisionByZeroReturnsNull;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() != 2)
        {
            throw new ExprValidationException("Arithmatic node must have 2 child nodes");
        }

        for (ExprNode child : this.getChildNodes())
        {
            Class childType = child.getExprEvaluator().getType();
            if (!JavaClassHelper.isNumeric(childType))
            {
                throw new ExprValidationException("Implicit conversion from datatype '" +
                        childType.getSimpleName() +
                        "' to numeric is not allowed");
            }
        }

        // Determine result type, set up compute function
        evaluatorLeft = this.getChildNodes().get(0).getExprEvaluator();
        evaluatorRight = this.getChildNodes().get(1).getExprEvaluator();

        Class childTypeOne = evaluatorLeft.getType();
        Class childTypeTwo = evaluatorRight.getType();

        if (childTypeOne.equals(childTypeTwo))
        {
            resultType = JavaClassHelper.getBoxedType(childTypeTwo);
        }
        else
        {
            resultType = JavaClassHelper.getArithmaticCoercionType(childTypeOne, childTypeTwo);
        }

        if ((mathArithTypeEnum == MathArithTypeEnum.DIVIDE) && (!isIntegerDivision))
        {
            if (resultType != BigDecimal.class)
            {
                resultType = Double.class;
            }            
        }

        arithTypeEnumComputer = mathArithTypeEnum.getComputer(resultType, childTypeOne, childTypeTwo, isIntegerDivision, isDivisionByZeroReturnsNull);
    }

    public Class getType()
    {
        return resultType;
    }

    public boolean isConstantResult()
    {
        return false;
    }
    
    public Map<String, Object> getEventType() {
        return null;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object valueChildOne = evaluatorLeft.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        if (valueChildOne == null)
        {
            return null;
        }

        Object valueChildTwo = evaluatorRight.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        if (valueChildTwo == null)
        {
            return null;
        }

        // arithTypeEnumComputer is initialized by validation
        return arithTypeEnumComputer.compute((Number) valueChildOne, (Number) valueChildTwo);
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append('(');

        buffer.append(this.getChildNodes().get(0).toExpressionString());
        buffer.append(mathArithTypeEnum.getExpressionText());
        buffer.append(this.getChildNodes().get(1).toExpressionString());

        buffer.append(')');
        return buffer.toString();
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprMathNode))
        {
            return false;
        }

        ExprMathNode other = (ExprMathNode) node;

        if (other.mathArithTypeEnum != this.mathArithTypeEnum)
        {
            return false;
        }

        return true;
    }

    /**
     * Returns the type of math.
     * @return math type
     */
    public MathArithTypeEnum getMathArithTypeEnum()
    {
        return mathArithTypeEnum;
    }
}
