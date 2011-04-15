/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.EPOnDemandQueryResult;
import com.espertech.esper.client.EPStatementException;
import com.espertech.esper.client.EPOnDemandPreparedQuery;
import com.espertech.esper.client.EventType;

/**
 * Provides prepared query functionality.
 */
public class EPPreparedQueryImpl implements EPOnDemandPreparedQuery
{
    private final EPPreparedExecuteMethod executeMethod;
    private final String epl;

    /**
     * Ctor.
     * @param executeMethod used at execution time to obtain query results
     * @param epl is the EPL to execute
     */
    public EPPreparedQueryImpl(EPPreparedExecuteMethod executeMethod, String epl)
    {
        this.executeMethod = executeMethod;
        this.epl = epl;
    }

    public EPOnDemandQueryResult execute()
    {
        try
        {
            EPPreparedQueryResult result = executeMethod.execute();
            return new EPQueryResultImpl(result);
        }
        catch (EPStatementException ex)
        {
            throw ex;
        }
        catch (Throwable t)
        {
            String message = "Error executing statement: " + t.getMessage();
            throw new EPStatementException(message, epl);
        }
    }

    public EventType getEventType()
    {
        return executeMethod.getEventType();
    }
}
