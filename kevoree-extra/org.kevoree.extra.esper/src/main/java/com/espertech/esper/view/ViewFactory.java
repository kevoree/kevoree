/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view;

import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.core.StatementContext;

import java.util.List;

/**
 * Factory interface for a factory responsible for creating a {@link View} instance and for determining
 * if an existing view meets requirements.
 */
public interface ViewFactory
{
    /**
     * Indicates user EPL query view parameters to the view factory.
     * @param viewFactoryContext supplied context information for the view factory
     * @param viewParameters is the objects representing the view parameters
     * @throws ViewParameterException if the parameters don't match view parameter needs
     */
    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> viewParameters) throws ViewParameterException;

    /**
     * Attaches the factory to a parent event type such that the factory can validate
     * attach requirements and determine an event type for resulting views.
     * @param parentEventType is the parent event stream's or view factory's event type
     * @param statementContext contains the services needed for creating a new event type
     * @param optionalParentFactory is null when there is no parent view factory, or contains the
     * parent view factory
     * @param parentViewFactories is a list of all the parent view factories or empty list if there are none
     * @throws ViewParameterException is thrown to indicate that this view factories's view would not play
     * with the parent view factories view
     */
    public void attach(EventType parentEventType,
                       StatementContext statementContext,
                       ViewFactory optionalParentFactory,
                       List<ViewFactory> parentViewFactories) throws ViewParameterException;

    /**
     * Returns true if the view factory can make views that provide a view resource with the
     * given capability.
     * @param viewCapability is the view resource needed
     * @return true to indicate that the view can provide the resource, or false if not
     */
    public boolean canProvideCapability(ViewCapability viewCapability);

    /**
     * Indicates to the view factory to provide the view resource indicated.
     * @param viewCapability is the required resource descriptor
     * @param resourceCallback is the callback to use to supply the resource needed
     */
    public void setProvideCapability(ViewCapability viewCapability, ViewResourceCallback resourceCallback);

    /**
     * Create a new view.
     * @param statementContext contains view services
     * @return new view
     */
    public View makeView(StatementContext statementContext);

    /**
     * Returns the event type that the view that is created by the view factory would create for events posted
     * by the view.
     * @return event type of view's created by the view factory
     */
    public EventType getEventType();

    /**
     * Determines if the given view could be used instead of creating a new view,
     * requires the view factory to compare view type, parameters and other capabilities provided.
     * @param view is the candidate view to compare to
     * @return true if the given view can be reused instead of creating a new view, or false to indicate
     * the view is not right for reuse
     */
    public boolean canReuse(View view);
}
