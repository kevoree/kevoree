/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client;

import com.espertech.esper.client.EventType;

/**
 * Interface for a prepared on-demand query that can be executed multiple times.
 */
public interface EPOnDemandPreparedQuery
{
    /**
     * Execute the prepared query returning query results.
     * @return query result
     */
    public EPOnDemandQueryResult execute();

    /**
     * Returns the event type, representing the columns of the select-clause.
     * @return event type
     */
    public EventType getEventType();
}
