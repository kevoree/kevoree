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
import com.espertech.esper.client.EventType;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.named.RemoveStreamViewCapability;
import com.espertech.esper.epl.expression.ExprNode;

import java.util.List;

/**
 * Factory for {@link com.espertech.esper.view.window.TimeAccumView}.
 */
public class TimeAccumViewFactory implements DataWindowViewFactory
{
    private EventType eventType;

    /**
     * Number of msec of quiet time before results are flushed.
     */
    protected long millisecondsQuietTime;

    /**
     * Access into the data window.
     */
    protected RandomAccessByIndexGetter randomAccessGetterImpl;

    /**
     * Indicators that we need to handle the remove stream posted by parent views.
     */
    protected boolean isRemoveStreamHandling;

    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> expressionParameters) throws ViewParameterException
    {
        List<Object> viewParameters = ViewFactorySupport.validateAndEvaluate("Time accumulative batch view", viewFactoryContext.getStatementContext(), expressionParameters);
        String errorMessage = "Time accumulative batch view requires a single numeric parameter or time period parameter";
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
                millisecondsQuietTime = Math.round(1000d * param.doubleValue());
            }
            else
            {
                millisecondsQuietTime = 1000 * param.longValue();
            }
        }

        if (millisecondsQuietTime < 1)
        {
            throw new ViewParameterException("Time accumulative batch view requires a size of at least 1 msec");
        }
    }

    public void attach(EventType parentEventType, StatementContext statementContext, ViewFactory optionalParentFactory, List<ViewFactory> parentViewFactories) throws ViewParameterException
    {
        this.eventType = parentEventType;
    }

    public boolean canProvideCapability(ViewCapability viewCapability)
    {
        if (viewCapability instanceof RemoveStreamViewCapability)
        {
            return true;
        }
        return viewCapability instanceof ViewCapDataWindowAccess;
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

        if (isRemoveStreamHandling)
        {
            return new TimeAccumViewRStream(this, statementContext, millisecondsQuietTime);
        }
        else
        {
            return new TimeAccumView(this, statementContext, millisecondsQuietTime, randomAccess);
        }
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
        if (!(view instanceof TimeAccumView))
        {
            return false;
        }

        TimeAccumView myView = (TimeAccumView) view;
        if (myView.getMsecIntervalSize() != millisecondsQuietTime)
        {
            return false;
        }

        return myView.isEmpty();
    }
}
