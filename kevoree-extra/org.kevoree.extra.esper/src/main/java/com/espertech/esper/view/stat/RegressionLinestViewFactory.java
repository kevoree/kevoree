/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.stat;

import com.espertech.esper.view.ViewFactory;
import com.espertech.esper.view.ViewParameterException;
import com.espertech.esper.view.*;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprNodeUtility;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.util.JavaClassHelper;

import java.util.List;

/**
 * Factory for {@link RegressionLinestView} instances.
 */
public class RegressionLinestViewFactory implements ViewFactory
{
    private List<ExprNode> viewParameters;

    /**
     * Expression X field.
     */
    protected ExprNode expressionX;

    /**
     * Expression Y field.
     */
    protected ExprNode expressionY;

    protected StatViewAdditionalProps additionalProps;

    protected EventType eventType;

    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> expressionParameters) throws ViewParameterException
    {
        this.viewParameters = expressionParameters;
    }

    public void attach(EventType parentEventType, StatementContext statementContext, ViewFactory optionalParentFactory, List<ViewFactory> parentViewFactories) throws ViewParameterException
    {
        ExprNode[] validated = ViewFactorySupport.validate("Correlation view", parentEventType, statementContext, viewParameters, false);
        String errorMessage = "Regression view requires two expressions providing x and y values as properties";

        if (validated.length < 2) {
            throw new ViewParameterException(errorMessage);
        }
        if ((!JavaClassHelper.isNumeric(validated[0].getExprEvaluator().getType())) || (!JavaClassHelper.isNumeric(validated[1].getExprEvaluator().getType())))
        {
            throw new ViewParameterException(errorMessage);
        }

        expressionX = validated[0];
        expressionY = validated[1];

        additionalProps = StatViewAdditionalProps.make(validated, 2);
        eventType = RegressionLinestView.createEventType(statementContext, additionalProps);
    }

    public boolean canProvideCapability(ViewCapability viewCapability)
    {
        return false;
    }

    public void setProvideCapability(ViewCapability viewCapability, ViewResourceCallback resourceCallback)
    {
        throw new UnsupportedOperationException("View capability " + viewCapability.getClass().getSimpleName() + " not supported");
    }

    public View makeView(StatementContext statementContext)
    {
        return new RegressionLinestView(statementContext, expressionX, expressionY, eventType, additionalProps);
    }

    public boolean canReuse(View view)
    {
        if (!(view instanceof RegressionLinestView))
        {
            return false;
        }

        if (additionalProps != null) {
            return false;
        }

        RegressionLinestView myView = (RegressionLinestView) view;
        if ((!ExprNodeUtility.deepEquals(myView.getExpressionX(), expressionX)) ||
            (!ExprNodeUtility.deepEquals(myView.getExpressionY(), expressionY)))
        {
            return false;
        }
        return true;
    }

    public EventType getEventType()
    {
        return eventType;
    }
}
