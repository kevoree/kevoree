/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import com.espertech.esper.pattern.MatchedEventMap;
import com.espertech.esper.util.MetaDefItem;

import java.io.Serializable;

/**
 * Interface for range-type filter parameters for type checking and to obtain the filter values for endpoints based
 * on prior results.
 */
public interface FilterSpecParamRangeValue extends MetaDefItem, Serializable
{
    /**
     * Returns the filter value representing the endpoint.
     * @param matchedEvents is the prior results
     * @return filter value
     */
    public Double getFilterValue(MatchedEventMap matchedEvents);

    /**
     * Returns a hash code for use in computing a filter range hash code that matches
     * when a same-values filter range is provided.
     * @return hash code or zero if no recomputable value can be computed
     */
    public int getFilterHash();
}
