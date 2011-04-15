/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.view.ViewFactoryChain;
import com.espertech.esper.view.ViewFactory;
import com.espertech.esper.view.ViewCapability;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.expression.ExprValidationException;

/**
 * Coordinates between view factories and requested resource (by expressions) the
 * availability of view resources to expressions.
 */
public class ViewResourceDelegateImpl implements ViewResourceDelegate
{
    private StatementContext statementContext;
    private ViewFactoryChain[] viewFactories;

    /**
     * Ctor.
     * @param viewFactories array of view factory chains, one for each stream
     * @param statementContext is statement-level services
     */
    public ViewResourceDelegateImpl(ViewFactoryChain[] viewFactories, StatementContext statementContext)
    {
        this.viewFactories = viewFactories;
        this.statementContext = statementContext;
    }

    public boolean requestCapability(int streamNumber, ViewCapability requestedCabability, ViewResourceCallback resourceCallback)
            throws ExprValidationException
    {
        ViewFactoryChain factories = viewFactories[streamNumber];

        // first we give the capability implementation a chance to inspect the view factory chain
        // it can deny by returning false
        if (!(requestedCabability.inspect(streamNumber, factories.getViewFactoryChain(), statementContext)))
        {
            return false;
        }

        // then ask each view in turn to support the capability
        boolean found = false;
        for (ViewFactory factory : factories.getViewFactoryChain())
        {
            if (factory.canProvideCapability(requestedCabability))
            {
                factory.setProvideCapability(requestedCabability, resourceCallback);
                found = true;
                if (!requestedCabability.appliesToChildViews())
                {
                    return true;
                }
            }
        }

        if (requestedCabability.appliesToChildViews())
        {
            return found;
        }

        // check if the capability requires child views
        if ((!requestedCabability.requiresChildViews()) && factories.getViewFactoryChain().isEmpty())
        {
            return true;
        }

        return false;
    }
}
