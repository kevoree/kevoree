/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.util.LikeUtil;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.schedule.TimeProvider;

import java.util.Map;

/**
 * Represents the like-clause in an expression tree.
 */
public class ExprLikeNode extends ExprNode implements ExprEvaluator
{
    private final boolean isNot;

    private boolean isNumericValue;
    private boolean isConstantPattern;

    private transient LikeUtil likeUtil;
    private transient ExprEvaluator[] evaluators;

    private static final long serialVersionUID = 34888860063217132L;

    /**
     * Ctor.
     * @param not is true if this is a "not like", or false if just a like
     */
    public ExprLikeNode(boolean not)
    {
        this.isNot = not;
    }

    public ExprEvaluator getExprEvaluator()
    {
        return this;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if ((this.getChildNodes().size() != 2) && (this.getChildNodes().size() != 3))
        {
            throw new ExprValidationException("The 'like' operator requires 2 (no escape) or 3 (with escape) child expressions");
        }
        evaluators = ExprNodeUtility.getEvaluators(this.getChildNodes());

        // check eval child node - can be String or numeric
        Class evalChildType = evaluators[0].getType();
        isNumericValue = JavaClassHelper.isNumeric(evalChildType);
        if ((evalChildType != String.class) && (!isNumericValue))
        {
            throw new ExprValidationException("The 'like' operator requires a String or numeric type left-hand expression");
        }

        // check pattern child node
        ExprEvaluator patternChildNode = evaluators[1];
        Class patternChildType = patternChildNode.getType();
        if (patternChildType != String.class)
        {
            throw new ExprValidationException("The 'like' operator requires a String-type pattern expression");
        }
        if (getChildNodes().get(1).isConstantResult())
        {
            isConstantPattern = true;
        }

        // check escape character node
        if (this.getChildNodes().size() == 3)
        {
            ExprEvaluator escapeChildNode = evaluators[2];
            if (escapeChildNode.getType() != String.class)
            {
                throw new ExprValidationException("The 'like' operator escape parameter requires a character-type value");
            }
        }
    }

    public Class getType()
    {
        return Boolean.class;
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
        if (likeUtil == null)
        {
            String patternVal = (String) evaluators[1].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            if (patternVal == null)
            {
                return null;
            }
            String escape = "\\";
            Character escapeCharacter = null;
            if (this.getChildNodes().size() == 3)
            {
                escape = (String) evaluators[2].evaluate(eventsPerStream, isNewData,exprEvaluatorContext);
            }
            if (escape.length() > 0)
            {
                escapeCharacter = escape.charAt(0);
            }
            likeUtil = new LikeUtil(patternVal, escapeCharacter, false);
        }
        else
        {
            if (!isConstantPattern)
            {
                String patternVal = (String) evaluators[1].evaluate(eventsPerStream, isNewData,exprEvaluatorContext);
                if (patternVal == null)
                {
                    return null;
                }
                likeUtil.resetPattern(patternVal);
            }
        }

        Object evalValue = evaluators[0].evaluate(eventsPerStream, isNewData,exprEvaluatorContext);
        if (evalValue == null)
        {
            return null;
        }

        if (isNumericValue)
        {
            evalValue = evalValue.toString();
        }

        Boolean result = likeUtil.compare( (String) evalValue);

        if (isNot)
        {
            return !result;
        }

        return result;
    }

    public boolean equalsNode(ExprNode node_)
    {
        if (!(node_ instanceof ExprLikeNode))
        {
            return false;
        }

        ExprLikeNode other = (ExprLikeNode) node_;
        if (this.isNot != other.isNot)
        {
            return false;
        }
        return true;
    }

    public String toExpressionString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.getChildNodes().get(0).toExpressionString());

        if (isNot)
        {
            buffer.append(" not");
        }

        buffer.append(" like ");
        buffer.append(this.getChildNodes().get(1).toExpressionString());

        if (this.getChildNodes().size() == 3)
        {
            buffer.append(" escape ");
            buffer.append(this.getChildNodes().get(2).toExpressionString());
        }

        return buffer.toString();
    }

    /**
     * Returns true if this is a "not like", or false if just a like
     * @return indicator whether negated or not
     */
    public boolean isNot()
    {
        return isNot;
    }
}
