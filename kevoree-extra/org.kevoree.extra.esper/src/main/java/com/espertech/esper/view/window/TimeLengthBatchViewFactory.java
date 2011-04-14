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
 * Factory for {@link com.espertech.esper.view.window.TimeLengthBatchView}.
 */
public class TimeLengthBatchViewFactory extends TimeBatchViewFactoryParams implements DataWindowViewFactory
{
    /**
     * Number of events to collect before batch fires.
     */
    protected long numberOfEvents;

    /**
     * The access into the data window.
     */
    protected RelativeAccessByEventNIndexMap relativeAccessGetterImpl;

    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> expressionParameters) throws ViewParameterException
    {
        List<Object> viewParameters = ViewFactorySupport.validateAndEvaluate("Time-length combination batch view", viewFactoryContext.getStatementContext(), expressionParameters);
        String errorMessage = "Time-length combination batch view requires a numeric or time period parameter as a time interval size, and an integer parameter as a maximal number-of-events, and an optional list of control keywords as a string parameter (please see the documentation)";
        if ((viewParameters.size() != 2) && (viewParameters.size() != 3))
        {
            throw new ViewParameterException(errorMessage);
        }

        // parameter 1
        processExpiry(viewParameters.get(0), errorMessage, "Time-length-combination batch view requires a size of at least 1 msec");

        // parameter 2
        Object parameter = viewParameters.get(1);
        if (!(parameter instanceof Number) || (JavaClassHelper.isFloatingPointNumber((Number) parameter)))
        {
            throw new ViewParameterException(errorMessage);
        }
        numberOfEvents = ((Number) parameter).longValue();

        if (viewParameters.size() > 2)
        {
            processKeywords(viewParameters.get(2), errorMessage);
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

        return new TimeLengthBatchView(this, statementContext, millisecondsBeforeExpiry, numberOfEvents, isForceUpdate, isStartEager, relativeAccessByEvent);
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
        if (!(view instanceof TimeLengthBatchView))
        {
            return false;
        }

        TimeLengthBatchView myView = (TimeLengthBatchView) view;

        if (myView.getMsecIntervalSize() != millisecondsBeforeExpiry)
        {
            return false;
        }

        if (myView.getNumberOfEvents() != numberOfEvents)
        {
            return false;
        }

        if (myView.isForceOutput() != isForceUpdate)
        {
            return false;
        }

        if (myView.isStartEager())  // since it's already started
        {
            return false;
        }

        return myView.isEmpty();
    }
}
