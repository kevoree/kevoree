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
import com.espertech.esper.util.MetaDefItem;

import java.io.Serializable;

/**
 * Represents a single item in a SELECT-clause, potentially unnamed
 * as no "as" tag may have been supplied in the syntax.
 * <p>
 * Compare to {@link SelectExprElementCompiledSpec} which carries a determined name.
 */
public class SelectExprElementRawSpec implements MetaDefItem, Serializable
{
    private ExprNode selectExpression;
    private String optionalAsName;
    private static final long serialVersionUID = -2943591690851195135L;

    /**
     * Ctor.
     * @param selectExpression - the expression node to evaluate for matching events
     * @param optionalAsName - the name of the item, null if not name supplied
     */
    public SelectExprElementRawSpec(ExprNode selectExpression, String optionalAsName)
    {
        this.selectExpression = selectExpression;
        this.optionalAsName = optionalAsName;
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
    public String getOptionalAsName()
    {
        return optionalAsName;
    }
}
