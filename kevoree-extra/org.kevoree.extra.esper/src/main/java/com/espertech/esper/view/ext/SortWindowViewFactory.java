/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.ext;

import com.espertech.esper.client.EventType;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.epl.named.RemoveStreamViewCapability;
import com.espertech.esper.view.*;
import com.espertech.esper.view.window.RandomAccessByIndexGetter;

import java.util.List;

/**
 * Factory for sort window views.
 */
public class SortWindowViewFactory implements DataWindowViewFactory
{
    private final static String NAME = "Sort view";

    private List<ExprNode> viewParameters;

    /**
     * The sort-by expressions.
     */
    protected ExprNode[] sortCriteriaExpressions;


    /**
     * The flags defining the ascending or descending sort order.
     */
    protected boolean[] isDescendingValues;

    /**
     * The sort window size.
     */
    protected int sortWindowSize;

    /**
     * The access into the collection for use with 'previous'.
     */
    protected RandomAccessByIndexGetter randomAccessGetterImpl;

    private EventType eventType;

    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> viewParams) throws ViewParameterException
    {
        this.viewParameters = viewParams;
    }

    public void attach(EventType parentEventType, StatementContext statementContext, ViewFactory optionalParentFactory, List<ViewFactory> parentViewFactories) throws ViewParameterException
    {
        eventType = parentEventType;
        String message = NAME + " requires a numeric size parameter and a list of expressions providing sort keys";
        if (viewParameters.size() < 2)
        {
            throw new ViewParameterException(message);
        }

        ExprNode[] validated = ViewFactorySupport.validate(NAME, parentEventType, statementContext, viewParameters, true);
        for (int i = 1; i < validated.length; i++)
        {
            ViewFactorySupport.assertReturnsNonConstant(NAME, validated[i], i);
        }

        Object sortSize = ViewFactorySupport.evaluateAssertNoProperties(NAME, validated[0], 0, statementContext);
        if ((sortSize == null) || (!(sortSize instanceof Number)))
        {
            throw new ViewParameterException(message);
        }
        sortWindowSize = ((Number) sortSize).intValue();

        sortCriteriaExpressions = new ExprNode[validated.length - 1];
        isDescendingValues = new boolean[sortCriteriaExpressions.length];

        for (int i = 1; i < validated.length; i++)
        {
            if (validated[i] instanceof ExprOrderedExpr)
            {
                isDescendingValues[i - 1] = ((ExprOrderedExpr) validated[i]).isDescending();
                sortCriteriaExpressions[i - 1] = validated[i].getChildNodes().get(0);
            }
            else
            {
                sortCriteriaExpressions[i - 1] = validated[i];
            }
        }
    }

    public boolean canProvideCapability(ViewCapability viewCapability)
    {
        if (viewCapability instanceof RemoveStreamViewCapability)
        {
            return true;
        }
        if (viewCapability instanceof ViewCapDataWindowAccess)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void setProvideCapability(ViewCapability viewCapability, ViewResourceCallback resourceCallback)
    {
        if (!canProvideCapability(viewCapability))
        {
            throw new UnsupportedOperationException("View capability " + viewCapability.getClass().getSimpleName() + " not supported");
        }
        if (viewCapability instanceof RemoveStreamViewCapability)
        {
            return;
        }
        if (randomAccessGetterImpl == null)
        {
            randomAccessGetterImpl = new RandomAccessByIndexGetter();
        }
        resourceCallback.setViewResource(randomAccessGetterImpl);
    }

    public View makeView(StatementContext statementContext)
    {
        IStreamSortedRandomAccess sortedRandomAccess = null;

        if (randomAccessGetterImpl != null)
        {
            sortedRandomAccess = new IStreamSortedRandomAccess(randomAccessGetterImpl);
            randomAccessGetterImpl.updated(sortedRandomAccess);
        }

        boolean useCollatorSort = false;
        if (statementContext.getConfigSnapshot() != null)
        {
            useCollatorSort = statementContext.getConfigSnapshot().getEngineDefaults().getLanguage().isSortUsingCollator();
        }

        ExprEvaluator[] childEvals = ExprNodeUtility.getEvaluators(sortCriteriaExpressions);
        return new SortWindowView(this, sortCriteriaExpressions, childEvals, isDescendingValues, sortWindowSize, sortedRandomAccess, useCollatorSort, statementContext);
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public boolean canReuse(View view)
    {
        if (randomAccessGetterImpl != null)
        {
            return false;
        }

        if (!(view instanceof SortWindowView))
        {
            return false;
        }

        SortWindowView other = (SortWindowView) view;
        if ((other.getSortWindowSize() != sortWindowSize) ||
            (!compare(other.getIsDescendingValues(), isDescendingValues)) ||
            (!ExprNodeUtility.deepEquals(other.getSortCriteriaExpressions(), sortCriteriaExpressions)) )
        {
            return false;
        }

        return other.isEmpty();
    }

    private boolean compare(boolean[] one, boolean[] two)
    {
        if (one.length != two.length)
        {
            return false;
        }

        for (int i = 0; i < one.length; i++)
        {
            if (one[i] != two[i])
            {
                return false;
            }
        }

        return true;
    }
}
