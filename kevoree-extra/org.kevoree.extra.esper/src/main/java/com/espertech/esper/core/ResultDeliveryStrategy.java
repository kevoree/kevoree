/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.UniformPair;

/**
 * Strategy for use with {@link StatementResultService} to dispatch to a statement's subscriber
 * via method invocations.
 */
public interface ResultDeliveryStrategy
{
    /**
     * Execute the dispatch.
     * @param result is the insert and remove stream to indicate
     */
    public void execute(UniformPair<EventBean[]> result);
}
