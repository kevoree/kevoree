/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.lookup;

import com.espertech.esper.client.EventBean;

import java.util.Collection;
import java.util.Set;

/**
 * Strategy for looking up, in some sort of table or index, or a set of events, potentially based on the
 * events properties, and returning a set of matched events.
 */
public interface TableLookupStrategy
{
    /**
     * Returns matched events for a set of events to look up for. Never returns an empty result set,
     * always returns null to indicate no results.
     * @param events to look up
     * @return set of matching events, or null if none matching
     */
    public Collection<EventBean> lookup(EventBean[] events);
}
