/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view;

import com.espertech.esper.client.EPException;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.view.internal.PriorEventViewFactory;
import com.espertech.esper.epl.expression.ExprConstantNode;
import com.espertech.esper.epl.expression.ExprNode;

import java.util.List;
import java.util.Arrays;

/**
 * Describes that we need access to prior events (result events published by views),
 * for use by the "prior" expression function.
 */
public class ViewCapPriorEventAccess implements ViewCapability
{
    private Integer indexConstant;

    /**
     * Ctor.
     * @param indexConstant is the index of the prior event, with zero being the current event.
     */
    public ViewCapPriorEventAccess(Integer indexConstant)
    {
        this.indexConstant = indexConstant;
    }

    /**
     * Index or the prior event we are asking for.
     * @return prior event index constant
     */
    public Integer getIndexConstant()
    {
        return indexConstant;
    }

    public boolean inspect(int streamNumber, List<ViewFactory> viewFactories, StatementContext statementContext)
    {
        boolean unboundStream = viewFactories.isEmpty();

        // Find the prior event view to see if it has already been added
        for (ViewFactory viewFactory : viewFactories)
        {
            if (viewFactory instanceof PriorEventViewFactory)
            {
                return true;
            }
        }

        try
        {
            String namespace = ViewEnum.PRIOR_EVENT_VIEW.getNamespace();
            String name = ViewEnum.PRIOR_EVENT_VIEW.getName();
            ViewFactory factory = statementContext.getViewResolutionService().create(namespace, name);
            viewFactories.add(factory);

            ViewFactoryContext context = new ViewFactoryContext(statementContext, streamNumber, viewFactories.size() + 1, namespace, name);
            factory.setViewParameters(context, Arrays.asList((ExprNode) new ExprConstantNode(unboundStream)));
        }
        catch (ViewProcessingException ex)
        {
            String text = "Exception creating prior event view factory";
            throw new EPException(text, ex);
        }
        catch (ViewParameterException ex)
        {
            String text = "Exception creating prior event view factory";
            throw new EPException(text, ex);
        }

        return true;
    }

    public boolean requiresChildViews()
    {
        return false;
    }

    public boolean appliesToChildViews()
    {
        return false;
    }    
}
