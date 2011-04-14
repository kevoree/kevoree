/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.expression;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.MethodResolutionService;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.ViewResourceDelegate;
import com.espertech.esper.epl.variable.VariableService;
import com.espertech.esper.schedule.TimeProvider;
import com.espertech.esper.util.JavaClassHelper;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represents the regexp-clause in an expression tree.
 */
public class ExprRegexpNode extends ExprNode implements ExprEvaluator
{
    private final boolean isNot;

    private Pattern pattern;
    private boolean isNumericValue;
    private boolean isConstantPattern;
    private transient ExprEvaluator[] evaluators;
    private static final long serialVersionUID = -837177267278295664L;

    /**
     * Ctor.
     * @param not is true if the it's a "not regexp" expression, of false for regular regexp
     */
    public ExprRegexpNode(boolean not)
    {
        this.isNot = not;
    }

    @Override
    public ExprEvaluator getExprEvaluator() {
        return this;
    }

    public Map<String, Object> getEventType() {
        return null;
    }

    public void validate(StreamTypeService streamTypeService, MethodResolutionService methodResolutionService, ViewResourceDelegate viewResourceDelegate, TimeProvider timeProvider, VariableService variableService, ExprEvaluatorContext exprEvaluatorContext) throws ExprValidationException
    {
        if (this.getChildNodes().size() != 2)
        {
            throw new ExprValidationException("The regexp operator requires 2 child expressions");
        }
        evaluators = ExprNodeUtility.getEvaluators(this.getChildNodes());

        // check pattern child node
        Class patternChildType = evaluators[1].getType();
        if (patternChildType != String.class)
        {
            throw new ExprValidationException("The regexp operator requires a String-type pattern expression");
        }
        if (this.getChildNodes().get(1).isConstantResult())
        {
            isConstantPattern = true;
        }

        // check eval child node - can be String or numeric
        Class evalChildType = evaluators[0].getType();
        isNumericValue = JavaClassHelper.isNumeric(evalChildType);
        if ((evalChildType != String.class) && (!isNumericValue))
        {
            throw new ExprValidationException("The regexp operator requires a String or numeric type left-hand expression");
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

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (pattern == null)
        {
            String patternText = (String) evaluators[1].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            if (patternText == null)
            {
                return null;
            }
            try
            {
                pattern = Pattern.compile(patternText);
            }
            catch (PatternSyntaxException ex)
            {
                throw new EPException("Error compiling regex pattern '" + patternText + '\'', ex);
            }
        }
        else
        {
            if (!isConstantPattern)
            {
                String patternText = (String) evaluators[1].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                if (patternText == null)
                {
                    return null;
                }
                try
                {
                    pattern = Pattern.compile(patternText);
                }
                catch (PatternSyntaxException ex)
                {
                    throw new EPException("Error compiling regex pattern '" + patternText + '\'', ex);
                }
            }
        }

        Object evalValue = evaluators[0].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        if (evalValue == null)
        {
            return null;
        }

        if (isNumericValue)
        {
            evalValue = evalValue.toString();
        }

        Boolean result = pattern.matcher((CharSequence) evalValue).matches();

        if (isNot)
        {
            return !result;
        }

        return result;
    }

    public boolean equalsNode(ExprNode node_)
    {
        if (!(node_ instanceof ExprRegexpNode))
        {
            return false;
        }

        ExprRegexpNode other = (ExprRegexpNode) node_;
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
        buffer.append(" regexp ");
        buffer.append(this.getChildNodes().get(1).toExpressionString());

        return buffer.toString();
    }

    /**
     * Returns true if this is a "not regexp", or false if just a regexp
     * @return indicator whether negated or not
     */
    public boolean isNot()
    {
        return isNot;
    }
}
