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

import java.util.List;

/**
 * Specification for the merge statement update-part.
 */
public class OnTriggerMergeItemUpdate extends OnTriggerMergeItem
{
    private static final long serialVersionUID = 726673263717907039L;
    
    private List<OnTriggerSetAssignment> assignments;

    public OnTriggerMergeItemUpdate(ExprNode optionalMatchCond, List<OnTriggerSetAssignment> assignments) {
        super(optionalMatchCond);
        this.assignments = assignments;
    }

    public List<OnTriggerSetAssignment> getAssignments() {
        return assignments;
    }
}

