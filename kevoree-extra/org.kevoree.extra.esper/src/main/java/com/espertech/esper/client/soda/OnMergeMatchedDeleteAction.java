/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import java.io.StringWriter;

/**
 * For use with on-merge clauses, deletes from a named window if matching rows are found.
 */
public class OnMergeMatchedDeleteAction implements OnMergeMatchedAction
{
    private static final long serialVersionUID = 0L;

    private Expression optionalCondition;

    /**
     * Ctor.
     * @param optionalCondition condition for action, or null if none required
     */
    public OnMergeMatchedDeleteAction(Expression optionalCondition) {
        this.optionalCondition = optionalCondition;
    }

    /**
     * Ctor.
     */
    public OnMergeMatchedDeleteAction() {
    }

    /**
     * Returns the action condition, or null if undefined.
     * @return condition
     */
    public Expression getOptionalCondition() {
        return optionalCondition;
    }

    /**
     * Sets the action condition, or null if undefined.
     * @param optionalCondition to set, or null to remove the condition
     */
    public void setOptionalCondition(Expression optionalCondition) {
        this.optionalCondition = optionalCondition;
    }

    @Override
    public void toEPL(StringWriter writer) {
        writer.write("when matched");
        if (optionalCondition != null) {
            writer.write(" and ");
            optionalCondition.toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
        }
        writer.write(" then delete");
    }
}