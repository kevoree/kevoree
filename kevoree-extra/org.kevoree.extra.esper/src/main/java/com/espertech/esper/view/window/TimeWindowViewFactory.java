/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.window;

import com.espertech.esper.view.*;
import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.named.RemoveStreamViewCapability;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.client.EventType;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.core.StatementContext;

import java.util.List;

/**
 * Factory for {@link TimeWindowView}.
 */
public class TimeWindowViewFactory implements DataWindowViewFactory
{
    /**
     * Number of msec before expiry.
     */
    protected long millisecondsBeforeExpiry;

    /**
     * Access into the data window.
     */
    protected RandomAccessByIndexGetter randomAccessGetterImpl;

    /**
     * Flag to indicate that the view must handle the removed events from a parent view.
     */
    protected boolean isRemoveStreamHandling;

    private EventType eventType;

    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> expressionParameters) throws ViewParameterException
    {
        List<Object> viewParameters = ViewFactorySupport.validateAndEvaluate("Time window view", viewFactoryContext.getStatementContext(), expressionParameters);
        String errorMessage = "Time window view requires a single numeric or time period parameter";
        if (viewParameters.size() != 1)
        {
            throw new ViewParameterException(errorMessage);
        }

        Object parameter = viewParameters.get(0);
        if (!(parameter instanceof Number))
        {
            throw new ViewParameterException(errorMessage);
        }
        else
        {
            Number param = (Number) parameter;
            if (JavaClassHelper.isFloatingPointNumber(param))
            {
                millisecondsBeforeExpiry = Math.round(1000d * param.doubleValue());
            }
            else
            {
                millisecondsBeforeExpiry = 1000 * param.longValue();
            }
        }

        if (millisecondsBeforeExpiry < 1)
        {
            throw new ViewParameterException("Time window view requires a size of at least 1 msec");
        }
    }

    public void attach(EventType parentEventType, StatementContext statementContext, ViewFactory optionalParentFactory, List<ViewFactory> parentViewFactories) throws ViewParameterException
    {
        this.eventType = parentEventType;
    }

    public boolean canProvideCapability(ViewCapability viewCapability)
    {
        if (viewCapability instanceof ViewCapDataWindowAccess)
        {
            return true;
        }
        else if (viewCapability instanceof RemoveStreamViewCapability)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns the number of millisecond before window contents expire.
     * @return num msec
     */
    public long getMillisecondsBeforeExpiry()
    {
        return millisecondsBeforeExpiry;
    }

    public void setProvideCapability(ViewCapability viewCapability, ViewResourceCallback resourceCallback)
    {
        if (!canProvideCapability(viewCapability))
        {
            throw new UnsupportedOperationException("View capability " + viewCapability.getClass().getSimpleName() + " not supported");
        }
        if (viewCapability instanceof RemoveStreamViewCapability)
        {
            isRemoveStreamHandling = true;
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
        IStreamRandomAccess randomAccess = null;

        if (randomAccessGetterImpl != null)
        {
            randomAccess = new IStreamRandomAccess(randomAccessGetterImpl);
            randomAccessGetterImpl.updated(randomAccess);
        }

        return new TimeWindowView(statementContext, this, millisecondsBeforeExpiry, randomAccess, isRemoveStreamHandling);
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

        if (!(view instanceof TimeWindowView))
        {
            return false;
        }

        TimeWindowView myView = (TimeWindowView) view;
        if (myView.getMillisecondsBeforeExpiry() != millisecondsBeforeExpiry)
        {
            return false;
        }

        // For reuse of the time window it doesn't matter if it provides random access or not
        return myView.isEmpty();
    }
}
