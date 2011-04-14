/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import java.util.Set;

/**
 * Service provider interface for filter service.
 */
public interface FilterServiceSPI extends FilterService
{
    /**
     * Take a set of statements of out the active filters, returning a save-set of filters.
     * @param statementId statement ids to remove
     * @return filters
     */
    public FilterSet take(Set<String> statementId);

    /**
     * Apply a set of previously taken filters.
     * @param filterSet to apply
     */
    public void apply(FilterSet filterSet);

    /**
     * Add activity listener.
     * @param filterServiceListener to add
     */
    public void addFilterServiceListener(FilterServiceListener filterServiceListener);

    /**
     * Remove activity listener.
     * @param filterServiceListener to remove
     */
    public void removeFilterServiceListener(FilterServiceListener filterServiceListener);
}