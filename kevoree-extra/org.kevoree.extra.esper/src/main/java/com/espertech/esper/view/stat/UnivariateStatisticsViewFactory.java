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
 * Factory for {@link UnivariateStatisticsView} instances.
 */
public class UnivariateStatisticsViewFactory implements ViewFactory
{
    private List<ExprNode> viewParameters;

    /**
     * Property name of data field.
     */
    protected ExprNode fieldExpression;
    protected StatViewAdditionalProps additionalProps;

    protected EventType eventType;

    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> expressionParameters) throws ViewParameterException
    {
        this.viewParameters = expressionParameters;
    }

    public void attach(EventType parentEventType, StatementContext statementContext, ViewFactory optionalParentFactory, List<ViewFactory> parentViewFactories) throws ViewParameterException
    {
        ExprNode[] validated = ViewFactorySupport.validate("Univariate statistics", parentEventType, statementContext, viewParameters, false);
        String errorMessage = "Univariate statistics view require a single expression returning a numeric value as a parameter";
        if (validated.length < 1) {
            throw new ViewParameterException(errorMessage);
        }
        if (!JavaClassHelper.isNumeric(validated[0].getExprEvaluator().getType()))
        {
            throw new ViewParameterException(errorMessage);
        }
        fieldExpression = validated[0];

        additionalProps = StatViewAdditionalProps.make(validated, 1);
        eventType = UnivariateStatisticsView.createEventType(statementContext, additionalProps);
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
        return new UnivariateStatisticsView(statementContext, fieldExpression, eventType, additionalProps);
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public boolean canReuse(View view)
    {
        if (!(view instanceof UnivariateStatisticsView)) {
            return false;
        }
        if (additionalProps != null) {
            return false;
        }

        UnivariateStatisticsView other = (UnivariateStatisticsView) view;
        if (!ExprNodeUtility.deepEquals(other.getFieldExpression(), fieldExpression))
        {
            return false;
        }

        return true;
    }
}
