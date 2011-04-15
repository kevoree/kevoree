/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.window;

import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.named.RemoveStreamViewCapability;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.client.EventType;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.*;

import java.util.List;

/**
 * Factory for {@link LengthWindowView}.
 */
public class LengthWindowViewFactory implements DataWindowViewFactory
{
    /**
     * Size of length window.
     */
    protected int size;

    /**
     * The access into the data window.
     */
    protected RandomAccessByIndexGetter randomAccessGetterImpl;

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
        if (viewCapability instanceof ViewCapDataWindowAccess)
        {
            return true;
        }
        if (viewCapability instanceof RemoveStreamViewCapability)
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
            return new LengthWindowViewRStream(this, size);
        }
        else
        {
            return new LengthWindowView(this, size, randomAccess);
        }
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public boolean canReuse(View view)
    {
        if (!(view instanceof LengthWindowView))
        {
            return false;
        }

        if (randomAccessGetterImpl != null)
        {
            return false;
        }

        LengthWindowView myView = (LengthWindowView) view;
        if (myView.getSize() != size)
        {
            return false;
        }
        return myView.isEmpty();
    }
}
