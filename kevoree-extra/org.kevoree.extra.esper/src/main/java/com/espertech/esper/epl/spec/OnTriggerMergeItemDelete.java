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
 * Specification for the merge statement delete-part.
 */
public class OnTriggerMergeItemDelete extends OnTriggerMergeItem
{
    private static final long serialVersionUID = 8183386154578818969L;

    public OnTriggerMergeItemDelete(ExprNode optionalMatchCond) {
        super(optionalMatchCond);
    }
}

