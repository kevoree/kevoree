package com.espertech.esper.view.internal;

import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.core.ViewResourceCallback;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.named.RemoveStreamViewCapability;
import com.espertech.esper.client.EventType;
import com.espertech.esper.view.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for union-views.
 */
public class UnionViewFactory implements ViewFactory, DataWindowViewFactory
{
    /**
     * The event type.
     */
    protected EventType parentEventType;

    /**
     * The view factories.
     */
    protected List<ViewFactory> viewFactories;

    /**
     * Ctor.
     * Dependencies injected after reflective instantiation.
     */
    public UnionViewFactory()
    {
    }

    /**
     * Sets the parent event type.
     * @param parentEventType type
     */
    public void setParentEventType(EventType parentEventType)
    {
        this.parentEventType = parentEventType;
    }

    /**
     * Sets the view factories.
     * @param viewFactories factories
     */
    public void setViewFactories(List<ViewFactory> viewFactories)
    {
        this.viewFactories = viewFactories;
    }

    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> viewParameters) throws ViewParameterException
    {
    }

    public void attach(EventType parentEventType, StatementContext statementContext, ViewFactory optionalParentFactory, List<ViewFactory> parentViewFactories) throws ViewParameterException
    {
    }

    public boolean canProvideCapability(ViewCapability viewCapability)
    {
        for (ViewFactory viewFactory : viewFactories)
        {
            if (!viewFactory.canProvideCapability(viewCapability))
            {
                return false;
            }
        }
        return viewCapability instanceof RemoveStreamViewCapability;
    }

    public void setProvideCapability(ViewCapability viewCapability, ViewResourceCallback resourceCallback)
    {
        if (!canProvideCapability(viewCapability))
        {
            throw new UnsupportedOperationException("View capability " + viewCapability.getClass().getSimpleName() + " not supported");
        }
        
        if (viewCapability instanceof RemoveStreamViewCapability)
        {
            for (ViewFactory viewFactory : viewFactories)
            {
                viewFactory.setProvideCapability(viewCapability, resourceCallback);
            }
        }
    }

    public View makeView(StatementContext statementContext)
    {
        boolean hasAsymetric = false;
        List<View> views = new ArrayList<View>();
        for (ViewFactory viewFactory : viewFactories)
        {
            views.add(viewFactory.makeView(statementContext));
            hasAsymetric |= viewFactory instanceof AsymetricDataWindowViewFactory;
        }
        if (hasAsymetric) {
            return new UnionAsymetricView(this, parentEventType, views);
        }
        return new UnionView(this, parentEventType, views);
    }

    public EventType getEventType()
    {
        return parentEventType;
    }

    public boolean canReuse(View view)
    {
        return false;
    }
}
