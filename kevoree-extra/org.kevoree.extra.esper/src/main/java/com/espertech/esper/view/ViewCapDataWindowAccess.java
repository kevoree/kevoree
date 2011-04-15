/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view;

import com.espertech.esper.view.internal.PriorEventViewFactory;
import com.espertech.esper.view.std.GroupByViewFactory;
import com.espertech.esper.core.StatementContext;

import java.util.List;

/**
 * Describes that we need random access into a data window by index.
 */
public class ViewCapDataWindowAccess implements ViewCapability
{
    /**
     * Ctor.
     */
    public ViewCapDataWindowAccess()
    {
    }

    public boolean inspect(int streamNumber, List<ViewFactory> viewFactories, StatementContext statementContext)
    {
        // We allow the capability only if
        //  - 1 view
        //  - 2 views and the first view is a group-by (for window-per-group access)
        if (viewFactories.size() == 1)
        {
            return true;
        }
        if (viewFactories.size() == 2)
        {
            if (viewFactories.get(0) instanceof GroupByViewFactory)
            {
                return true;
            }
            if (viewFactories.get(1) instanceof PriorEventViewFactory) {
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean requiresChildViews()
    {
        return true;
    }

    public boolean appliesToChildViews()
    {
        return false;
    }
}
