/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.named;

import com.espertech.esper.view.ViewCapability;
import com.espertech.esper.view.ViewFactory;
import com.espertech.esper.view.DataWindowViewFactory;
import com.espertech.esper.view.std.GroupByViewFactory;
import com.espertech.esper.view.std.MergeViewFactory;
import com.espertech.esper.core.StatementContext;
import com.espertech.esper.epl.expression.ExprValidationException;

import java.util.List;

/**
 * Expresses the requirement that all views are derived-value views and now data window views, with the exception of
 * group-by and merge views.
 */
public class NotADataWindowViewCapability implements ViewCapability
{
    public boolean inspect(int streamNumber, List<ViewFactory> viewFactories, StatementContext statementContext)
            throws ExprValidationException
    {
        for (ViewFactory viewFactory : viewFactories)
        {
            if ((viewFactory instanceof GroupByViewFactory) || ((viewFactory instanceof MergeViewFactory)))
            {
                continue;
            }
            if (viewFactory instanceof DataWindowViewFactory)
            {
                throw new ExprValidationException(NamedWindowService.ERROR_MSG_NO_DATAWINDOW_ALLOWED);
            }
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
