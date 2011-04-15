/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.std;

import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprNodeUtility;
import com.espertech.esper.epl.named.RemoveStreamViewCapability;
import com.espertech.esper.client.EventType;
import com.espertech.esper.view.*;

import java.util.List;

/**
 * Factory for {@link FirstUniqueByPropertyView} instances.
 */
public class FirstUniqueByPropertyViewFactory implements AsymetricDataWindowViewFactory
{
    /**
     * View parameters.
     */
    protected List<ExprNode> viewParameters;

    /**
     * Property name to evaluate unique values.
     */
    protected ExprNode[] criteriaExpressions;

    private EventType eventType;

    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> expressionParameters) throws ViewParameterException
    {
        this.viewParameters = expressionParameters;
    }

    public void attach(EventType parentEventType, StatementContext statementContext, ViewFactory optionalParentFactory, List<ViewFactory> parentViewFactories) throws ViewParameterException
    {
        criteriaExpressions = ViewFactorySupport.validate("First-unique-by view", parentEventType, statementContext, viewParameters, false);

        if (criteriaExpressions.length == 0)
        {
            String errorMessage = "First-unique-by view requires a one or more expressions provinding unique values as parameters";
            throw new ViewParameterException(errorMessage);
        }

        this.eventType = parentEventType;
    }

    public boolean canProvideCapability(ViewCapability viewCapability)
    {
        if (viewCapability instanceof RemoveStreamViewCapability)
        {
            return true;
        }
        return false;
    }

    public void setProvideCapability(ViewCapability viewCapability, ViewResourceCallback resourceCallback)
    {
        if (viewCapability instanceof RemoveStreamViewCapability)
        {
            return;
        }
        throw new UnsupportedOperationException("View capability " + viewCapability.getClass().getSimpleName() + " not supported");
    }

    public View makeView(StatementContext statementContext)
    {
        return new FirstUniqueByPropertyView(criteriaExpressions, statementContext);
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public boolean canReuse(View view)
    {
        if (!(view instanceof FirstUniqueByPropertyView))
        {
            return false;
        }

        FirstUniqueByPropertyView myView = (FirstUniqueByPropertyView) view;
        if (!ExprNodeUtility.deepEquals(criteriaExpressions, myView.getUniqueCriteria()))
        {
            return false;
        }

        return myView.isEmpty();
    }
}
