/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.stream;

import com.espertech.esper.event.EventAdapterService;

/**
 * Static factory for implementations of the StreamFactoryService interface.
 */
public final class StreamFactoryServiceProvider
{
    /**
     * Creates an implementation of the StreamFactoryService interface.
     * @param isReuseViews indicator on whether stream and view resources are to be reused between statements
     * @return implementation
     */
    public static StreamFactoryService newService(boolean isReuseViews)
    {
        return new StreamFactorySvcImpl(isReuseViews);
    }
}
