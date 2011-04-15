/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.internal;

import com.espertech.esper.client.EventType;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.view.*;
import com.espertech.esper.view.window.RelativeAccessByEventNIndex;
import com.espertech.esper.collection.*;
import com.espertech.esper.core.StatementContext;

import java.util.*;

/**
 * Factory for making {@link PriorEventView} instances.
 */
public class PriorEventViewFactory implements ViewFactory
{
    /**
     * Map of prior-index and callback to expressions.
     */
    protected TreeMap<Integer, List<ViewResourceCallback>> callbacksPerIndex = new TreeMap<Integer, List<ViewResourceCallback>>();

    private EventType eventType;

    /**
     * unbound to indicate the we are not receiving remove stream events (unbound stream, stream without child
     * views) therefore must use a different buffer.
     */
    protected boolean isUnbound;

    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> expressionParameters) throws ViewParameterException
    {
        List<Object> viewParameters = ViewFactorySupport.validateAndEvaluate("Prior event view", viewFactoryContext.getStatementContext(), expressionParameters);
        if (viewParameters.size() != 1)
        {
            throw new ViewParameterException("View requires a single parameter indicating unbound or not");
        }
        isUnbound = (Boolean) viewParameters.get(0);
    }

    public void attach(EventType parentEventType, StatementContext statementContext, ViewFactory optionalParentFactory, List<ViewFactory> parentViewFactories) throws ViewParameterException
    {
        eventType = parentEventType;
    }

    public boolean canProvideCapability(ViewCapability viewCapability)
    {
        if (viewCapability instanceof ViewCapPriorEventAccess)
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

        // Get the index requested, such as the 8th prior event
        ViewCapPriorEventAccess requested = (ViewCapPriorEventAccess) viewCapability;
        int reqIndex = requested.getIndexConstant();

        // Store in a list per index such that we can consolidate this into a single buffer
        List<ViewResourceCallback> callbackList = callbacksPerIndex.get(reqIndex);
        if (callbackList == null)
        {
            callbackList = new LinkedList<ViewResourceCallback>();
            callbacksPerIndex.put(reqIndex, callbackList);
        }
        callbackList.add(resourceCallback);
    }

    public View makeView(StatementContext statementContext)
    {
        ViewUpdatedCollection viewUpdatedCollection;

        if (callbacksPerIndex.isEmpty())
        {
            throw new IllegalStateException("No resources requested");
        }

        // Construct an array of requested prior-event indexes (such as 10th prior event, 8th prior = {10, 8})
        int[] requested = new int[callbacksPerIndex.size()];
        int count = 0;
        for (int reqIndex : callbacksPerIndex.keySet())
        {
            requested[count++] = reqIndex;
        }


        // For unbound streams the buffer is strictly rolling new events
        if (isUnbound)
        {
            viewUpdatedCollection = new PriorEventBufferUnbound(callbacksPerIndex.lastKey());
        }
        // For bound streams (with views posting old and new data), and if only one prior index requested
        else if (requested.length == 1)
        {
            viewUpdatedCollection = new PriorEventBufferSingle(requested[0]);
        }
        else
        {
            // For bound streams (with views posting old and new data)
            // Multiple prior event indexes requested, such as "prior(2, price), prior(8, price)"
            // Sharing a single viewUpdatedCollection for multiple prior-event indexes
            viewUpdatedCollection = new PriorEventBufferMulti(requested);
        }

        // Since an expression such as "prior(2, price), prior(8, price)" translates
        // into {2, 8} the relative index is {0, 1}.
        // Map the expression-supplied index to a relative viewUpdatedCollection-known index via wrapper
        int relativeIndex = 0;
        for (int reqIndex : callbacksPerIndex.keySet())
        {
            List<ViewResourceCallback> callbacks = callbacksPerIndex.get(reqIndex);
            for (ViewResourceCallback callback : callbacks)
            {
                if (viewUpdatedCollection instanceof RelativeAccessByEventNIndex)
                {
                    RelativeAccessByEventNIndex relativeAccess = (RelativeAccessByEventNIndex) viewUpdatedCollection;
                    callback.setViewResource(new RelativeAccessImpl(relativeAccess, relativeIndex));
                }
                else
                {
                    callback.setViewResource(viewUpdatedCollection);
                }
            }
            relativeIndex++;
        }

        return new PriorEventView(viewUpdatedCollection);
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public boolean canReuse(View view)
    {
        return false;
    }

    /**
     * Adapter to provide access given an index.
     */
    public static class RelativeAccessImpl implements RelativeAccessByEventNIndex
    {
        private final RelativeAccessByEventNIndex buffer;
        private final int relativeIndex;

        /**
         * Ctor.
         * @param buffer is the buffer to acces
         * @param relativeIndex is the index to pull out
         */
        public RelativeAccessImpl(RelativeAccessByEventNIndex buffer, int relativeIndex)
        {
            this.buffer = buffer;
            this.relativeIndex = relativeIndex;
        }

        public EventBean getRelativeToEvent(EventBean event, int prevIndex)
        {
            return buffer.getRelativeToEvent(event, relativeIndex);
        }

        public EventBean getRelativeToEnd(EventBean event, int index)
        {
            // No requirement to index from end of current buffer
            return null;
        }

        public Iterator<EventBean> getWindowToEvent(Object evalEvent)
        {
            return null;
        }

        public int getWindowToEventCount(EventBean evalEvent)
        {
            return 0;
        }
    }
}
