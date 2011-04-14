/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.agg;

import com.espertech.esper.epl.core.MethodResolutionService;

/**
 * For testing if a remove stream entry has been present.
 */
public class LeavingAggregator implements AggregationMethod {

    private boolean leaving = false;

    public void enter(Object value) {
    }

    public void leave(Object value) {
        leaving = true;
    }

    public Class getValueType() {
        return Boolean.class;
    }

    public Object getValue() {
        return leaving;
    }

    public void clear() {
        leaving = false;
    }

    public AggregationMethod newAggregator(MethodResolutionService methodResolutionService) {
        return methodResolutionService.makeLeavingAggregator();
    }
}