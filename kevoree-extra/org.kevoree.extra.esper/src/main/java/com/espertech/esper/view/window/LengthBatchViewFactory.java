/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.window;

import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.named.RemoveStreamViewCapability;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.client.EventType;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.*;
import com.espertech.esper.core.StatementContext;

import java.util.List;

/**
 * Factory for {@link com.espertech.esper.view.window.TimeBatchView}.
 */
public class LengthBatchViewFactory implements DataWindowViewFactory
{
    /**
     * The length window size.
     */
    protected int size;

    /**
     * The access into the window.
     */
    protected RelativeAccessByEventNIndexMap relativeAccessGetterImpl;

    /**
     * Flag to indicate that the view must handle the removed events from a parent view.
     */
    protected boolean isRemoveStreamHandling;

    private EventType eventType;

    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> expressionParameters) throws ViewParameterException
    {
        List<Object> viewParameters = ViewFactorySupport.validateAndEvaluate("Length window view", viewFactoryContext.getStatementContext(), expressionParameters);
        String errorMessage = "Length window view requires a single integer-type parameter";
        if (viewParameters.size() != 1)
        {
            throw new ViewParameterException(errorMessage);
        }

        Object parameter = viewParameters.get(0);
        if (!(parameter instanceof Number))
        {
            throw new ViewParameterException(errorMessage);
        }
        Number numParam = (Number) parameter;
        if ( (JavaClassHelper.isFloatingPointNumber(numParam)) ||
             (numParam instanceof Long))
        {
            throw new ViewParameterException(errorMessage);
        }

        size =  numParam.intValue();
        if (size <= 0)
        {
            throw new ViewParameterException("Length window requires a positive number");
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
        if (relativeAccessGetterImpl == null)
        {
            relativeAccessGetterImpl = new RelativeAccessByEventNIndexMap();
        }
        resourceCallback.setViewResource(relativeAccessGetterImpl);
    }

    public View makeView(StatementContext statementContext)
    {
        IStreamRelativeAccess relativeAccessByEvent = null;

        if (relativeAccessGetterImpl != null)
        {
            relativeAccessByEvent = new IStreamRelativeAccess(relativeAccessGetterImpl);
            relativeAccessGetterImpl.updated(relativeAccessByEvent, null);
        }

        if (isRemoveStreamHandling)
        {
            return new LengthBatchViewRStream(this, size);
        }
        else
        {
            return new LengthBatchView(this, size, relativeAccessByEvent);
        }
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public boolean canReuse(View view)
    {
        if (relativeAccessGetterImpl != null)
        {
            return false;
        }
        if (!(view instanceof LengthBatchView))
        {
            return false;
        }

        LengthBatchView myView = (LengthBatchView) view;
        if (myView.getSize() != size)
        {
            return false;
        }

        return myView.isEmpty();
    }
}
