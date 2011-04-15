/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view;

import com.espertech.esper.client.EventType;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.core.StreamTypeService;
import com.espertech.esper.epl.core.StreamTypeServiceImpl;
import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprNodeSummaryVisitor;
import com.espertech.esper.epl.expression.ExprValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for view factories that do not make re-useable views and that do
 * not share view resources with expression nodes.
 */
public abstract class ViewFactorySupport implements ViewFactory
{
    private static Log log = LogFactory.getLog(ViewFactorySupport.class);

    public boolean canProvideCapability(ViewCapability viewCapability)
    {
        return false;
    }

    public void setProvideCapability(ViewCapability viewCapability, ViewResourceCallback resourceCallback)
    {
        throw new UnsupportedOperationException("View capability " + viewCapability.getClass().getSimpleName() + " not supported");
    }

    public boolean canReuse(View view)
    {
        return false;
    }

    /**
     * Validate the view parameter expression and evaluate the expression returning the result object.
     * @param viewName textual name of view
     * @param statementContext context with statement services
     * @param expression view expression parameter to validate
     * @return object result value of parameter expression
     * @throws ViewParameterException if the expressions fail to validate
     */
    public static Object validateAndEvaluate(String viewName, StatementContext statementContext, ExprNode expression)
            throws ViewParameterException
    {
        return validateAndEvaluateExpr(statementContext, expression, new StreamTypeServiceImpl(statementContext.getEngineURI(), false), 0);
    }

    /**
     * Validate the view parameter expressions and evaluate the expressions returning the result object.
     * @param viewName textual name of view
     * @param statementContext context with statement services
     * @param expressions view expression parameter to validate
     * @return object result value of parameter expressions
     * @throws ViewParameterException if the expressions fail to validate
     */
    public static List<Object> validateAndEvaluate(String viewName, StatementContext statementContext, List<ExprNode> expressions)
            throws ViewParameterException
    {
        List<Object> results = new ArrayList<Object>();
        int expressionNumber = 0;
        StreamTypeService streamTypeService = new StreamTypeServiceImpl(statementContext.getEngineURI(), false);
        for (ExprNode expr : expressions)
        {
            Object result = validateAndEvaluateExpr(statementContext, expr, streamTypeService, expressionNumber);
            results.add(result);
            expressionNumber++;
        }
        return results;
    }

    /**
     * Validate the view parameter expressions and return the validated expression for later execution.
     * <p>
     * Does not evaluate the expression.
     * @param viewName textual name of view
     * @param eventType is the event type of the parent view or stream attached.
     * @param statementContext context with statement services
     * @param expressions view expression parameter to validate
     * @param allowConstantResult true to indicate whether expressions that return a constant
     * result should be allowed; false to indicate that if an expression is known to return a constant result
     * the expression is considered invalid
     * @return object result value of parameter expressions
     * @throws ViewParameterException if the expressions fail to validate
     */
    public static ExprNode[] validate(String viewName, EventType eventType, StatementContext statementContext, List<ExprNode> expressions, boolean allowConstantResult)
            throws ViewParameterException
    {
        List<ExprNode> results = new ArrayList<ExprNode>();
        int expressionNumber = 0;
        StreamTypeService streamTypeService = new StreamTypeServiceImpl(eventType, null, false, statementContext.getEngineURI());
        for (ExprNode expr : expressions)
        {
            ExprNode validated = validateExpr(statementContext, expr, streamTypeService, expressionNumber);
            results.add(validated);
            
            if ((!allowConstantResult) && (validated.isConstantResult()))
            {
                String message = "Invalid view parameter expression " + expressionNumber + ", the expression returns a constant result value, are you sure?";
                log.error(message);
                throw new ViewParameterException(message);
            }

            expressionNumber++;
        }
        return results.toArray(new ExprNode[results.size()]);
    }

    /**
     * Assert and throws an exception if the expression passed returns a non-constant value.
     * @param viewName textual name of view
     * @param expression expression to check
     * @param index number offset of expression in view parameters
     * @throws ViewParameterException if assertion fails
     */
    public static void assertReturnsNonConstant(String viewName, ExprNode expression, int index) throws ViewParameterException
    {
        if (expression.isConstantResult())
        {
            String message = "Invalid view parameter expression " + index + ", the expression returns a constant result value, are you sure?";
            log.error(message);
            throw new ViewParameterException(message);
        }
    }

    /**
     * Assert and throws an exception if the expression uses event property values.
     * @param viewName textual name of view
     * @param expression expression to check
     * @param index number offset of expression in view parameters
     * @param exprEvaluatorContext context for expression evaluation
     * @return expression evaluation value
     * @throws ViewParameterException if assertion fails
     */
    public static Object evaluateAssertNoProperties(String viewName, ExprNode expression, int index, ExprEvaluatorContext exprEvaluatorContext) throws ViewParameterException
    {
        ExprNodeSummaryVisitor visitor = new ExprNodeSummaryVisitor();
        expression.accept(visitor);
        if (!visitor.isPlain())
        {
            String message = "Invalid view parameter expression " + index + ", " + visitor.getMessage() + " are not allowed within the expression";
            log.error(message);
            throw new ViewParameterException(message);
        }

        return expression.getExprEvaluator().evaluate(null, false, exprEvaluatorContext);
    }

    private static Object validateAndEvaluateExpr(StatementContext statementContext, ExprNode expression, StreamTypeService streamTypeService, int expressionNumber)
            throws ViewParameterException
    {
        ExprNode validated = validateExpr(statementContext, expression, streamTypeService, expressionNumber);

        try
        {
            return validated.getExprEvaluator().evaluate(null, true, statementContext);
        }
        catch (RuntimeException ex)
        {
            String message = "Failed to evaluate parameter expression " + expressionNumber;
            if (ex.getMessage() != null)
            {
                message += ": " + ex.getMessage();
            }
            log.error(message, ex);
            throw new ViewParameterException(message);
        }
    }

    private static ExprNode validateExpr(StatementContext statementContext, ExprNode expression, StreamTypeService streamTypeService, int expressionNumber)
            throws ViewParameterException
    {
        ExprNode validated;
        try
        {
            validated = expression.getValidatedSubtree(streamTypeService, statementContext.getMethodResolutionService(),
                    null, statementContext.getSchedulingService(), statementContext.getVariableService(), statementContext);
        }
        catch (ExprValidationException ex)
        {
            String message = "Invalid parameter expression " + expressionNumber;
            if (ex.getMessage() != null)
            {
                message += ": " + ex.getMessage();
            }
            log.error(message, ex);
            throw new ViewParameterException(message);
        }
        return validated;
    }
}
