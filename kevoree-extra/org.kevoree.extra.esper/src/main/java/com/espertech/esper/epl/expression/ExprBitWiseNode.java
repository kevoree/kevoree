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
import com.espertech.esper.type.BitWiseOpEnum;
import com.espertech.esper.util.JavaClassHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Represents the bit-wise operators in an expression tree.
 */
public class ExprBitWiseNode extends ExprNode implements ExprEvaluator {

    private final BitWiseOpEnum _bitWiseOpEnum;
    private transient BitWiseOpEnum.Computer bitWiseOpEnumComputer;
    private Class resultType;

    private transient ExprEvaluator[] evaluators;

    private static final long serialVersionUID = 9035943176810365437L;

    /**
     * Ctor.
     * @param bitWiseOpEnum_ - type of math
     */
    public ExprBitWiseNode(BitWiseOpEnum bitWiseOpEnum_)
    {
        _bitWiseOpEnum = bitWiseOpEnum_;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    /**
     * Returns the bitwise operator.
     * @return operator
     */
    public BitWiseOpEnum getBitWiseOpEnum()
    {
        return _bitWiseOpEnum;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() != 2)
        {
            throw new ExprValidationException("BitWise node must have 2 child nodes");
        }

        evaluators = ExprNodeUtility.getEvaluators(this.getChildNodes());
        for (ExprEvaluator child : evaluators)
        {
            Class childType = child.getType();
            if ((!JavaClassHelper.isBoolean(childType)) && (!JavaClassHelper.isNumeric(childType)))
            {
                throw new ExprValidationException("Invalid datatype for bitwise " +
                        childType.getName() + " is not allowed");
            }
        }

        // Determine result type, set up compute function
        Class childTypeOne = evaluators[0].getType();
        Class childTypeTwo = evaluators[1].getType();
        if ((JavaClassHelper.isFloatingPointClass(childTypeOne)) || (JavaClassHelper.isFloatingPointClass(childTypeTwo)))
        {
            throw new ExprValidationException("Invalid type for bitwise " + _bitWiseOpEnum.getComputeDescription()  + " operator");
        }
        else
        {
            Class childBoxedTypeOne = JavaClassHelper.getBoxedType(childTypeOne) ;
            Class childBoxedTypeTwo = JavaClassHelper.getBoxedType(childTypeTwo) ;
            if (childBoxedTypeOne == childBoxedTypeTwo)
            {
                resultType = childBoxedTypeOne;
                bitWiseOpEnumComputer = _bitWiseOpEnum.getComputer(resultType);
            }
            else
            {
                throw new ExprValidationException("Both nodes muts be of the same type for bitwise " + _bitWiseOpEnum.getComputeDescription()  + " operator");
            }
        }
    }

    public boolean isConstantResult()
    {
        return false;
    }

    public Class getType()
    {
        return resultType;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        Object valueChildOne = evaluators[0].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        Object valueChildTwo = evaluators[1].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

        if ((valueChildOne == null) || (valueChildTwo == null))
        {
            return null;
        }

        // bitWiseOpEnumComputer is initialized by validation
        return bitWiseOpEnumComputer.compute(valueChildOne, valueChildTwo);
    }

    public boolean equalsNode(ExprNode node)
    {
        if (!(node instanceof ExprBitWiseNode))
        {
            return false;
        }

        ExprBitWiseNode other = (ExprBitWiseNode) node;

        if (other._bitWiseOpEnum != _bitWiseOpEnum)
        {
            return false;
        }

        return true;
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append('(');

        buffer.append(getChildNodes().get(0).toExpressionString());
        buffer.append(_bitWiseOpEnum.getComputeDescription());
        buffer.append(getChildNodes().get(1).toExpressionString());

        buffer.append(')');
        return buffer.toString();
    }

    private static final Log log = LogFactory.getLog(ExprBitWiseNode.class);
}
