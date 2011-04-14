/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.epl.expression.ExprNode;

/**
 * Represents a single item in a SELECT-clause, with a name assigned
 * either by the engine or by the user specifying an "as" tag name.
 */
public class SelectExprElementCompiledSpec
{
    private ExprNode selectExpression;
    private String assignedName;

    /**
     * Ctor.
     * @param selectExpression - the expression node to evaluate for matching events
     * @param assignedName - cannot be null as a name is always assigned or
     * system-determined
     */
    public SelectExprElementCompiledSpec(ExprNode selectExpression, String assignedName)
    {
        this.selectExpression = selectExpression;
        this.assignedName = assignedName;
    }

    /**
     * Returns the expression node representing the item in the select clause.
     * @return expression node for item
     */
    public ExprNode getSelectExpression()
    {
        return selectExpression;
    }

    /**
     * Returns the name of the item in the select clause.
     * @return name of item
     */
    public String getAssignedName()
    {
        return assignedName;
    }
}
