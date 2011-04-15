/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import java.util.List;

/**
 * Specification for the merge statement.
 */
public class OnTriggerMergeDesc extends OnTriggerWindowDesc
{
    private static final long serialVersionUID = 3388811105339812571L;
    
    private List<OnTriggerMergeItem> items;

    public OnTriggerMergeDesc(String windowName, String optionalAsName, List<OnTriggerMergeItem> items) {
        super(windowName, optionalAsName, OnTriggerType.ON_MERGE);
        this.items = items;
    }

    public List<OnTriggerMergeItem> getItems() {
        return items;
    }
}

