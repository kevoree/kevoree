/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.named;

import com.espertech.esper.core.EPStatementHandle;

import java.util.List;
import java.util.Map;


/**
 * Holds a unit of dispatch that is a result of a named window processing incoming or timer events.
 */
public class NamedWindowConsumerDispatchUnit
{
    private NamedWindowDeltaData deltaData;
    private Map<EPStatementHandle, List<NamedWindowConsumerView>> dispatchTo;

    /**
     * Ctor.
     * @param deltaData the insert and remove stream posted by the named window
     * @param dispatchTo the list of consuming statements, and for each the list of consumer views
     */
    public NamedWindowConsumerDispatchUnit(NamedWindowDeltaData deltaData, Map<EPStatementHandle, List<NamedWindowConsumerView>> dispatchTo)
    {
        this.deltaData = deltaData;
        this.dispatchTo = dispatchTo;
    }

    /**
     * Returns the data to dispatch.
     * @return dispatch insert and remove stream events
     */
    public NamedWindowDeltaData getDeltaData()
    {
        return deltaData;
    }

    /**
     * Returns the destination of the dispatch: a map of statements and their consuming views (one or multiple)
     * @return map of statement to consumer views
     */
    public Map<EPStatementHandle, List<NamedWindowConsumerView>> getDispatchTo()
    {
        return dispatchTo;
    }
}
