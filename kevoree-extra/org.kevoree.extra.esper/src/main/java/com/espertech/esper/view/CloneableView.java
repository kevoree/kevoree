/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view;

import com.espertech.esper.core.StatementContext;

/**
 * Views that can work under a group-by must be able to duplicate and are required to implement this interface.
 */
public interface CloneableView
{
    /**
     * Duplicates the view.
     * <p>
     * Expected to return a same view in initialized state for grouping.
     * @param statementContext is services for the view
     * @return duplicated view
     */
    public View cloneView(StatementContext statementContext);
}
