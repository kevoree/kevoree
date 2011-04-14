/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.agg;

import com.espertech.esper.collection.MultiKeyUntyped;
import com.espertech.esper.epl.core.MethodResolutionService;

/**
 * Utility for use with aggregation access functions.
 */
public class AggregationAccessUtil
{
    /**
     * Returns new accesses for each function.
     * @param isJoin true for joins
     * @param streams stream numbers
     * @param methodResolutionService service for obtaining accesses
     * @param groupKey group by key
     * @return array of accessors
     */
    protected static AggregationAccess[] getNewAccesses(boolean isJoin, int[] streams, MethodResolutionService methodResolutionService, MultiKeyUntyped groupKey) {
        AggregationAccess[] row = new AggregationAccess[streams.length];
        int i = 0;
        for (int stream : streams) {
            row[i] = methodResolutionService.makeAccessStreamId(isJoin, stream, groupKey);
            i++;
        }
        return row;
    }
}