/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

/**
 * Static factory for implementations of the {@link FilterService} interface.
 */
public final class FilterServiceProvider
{
    /**
     * Creates an implementation of the FilterEvaluationService interface.
     * @return implementation
     */
    public static FilterServiceSPI newService()
    {
        return new FilterServiceImpl();
    }
}
