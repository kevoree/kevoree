/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPOnDemandPreparedQuery;
import com.espertech.esper.client.EPOnDemandQueryResult;

import java.util.Map;

/**
 * SPI interface of the runtime exposes fire-and-forget, non-continuous query functionality.
 */
public interface EPRuntimeSPI extends EPRuntime
{
    /**
     * Returns all declared variable names and their types.
     * @return variable names and types
     */
    public Map<String, Class> getVariableTypeAll();

    /**
     * Returns a variable's type.
     * @param variableName type or null if the variable is not declared
     * @return type of variable
     */
    public Class getVariableType(String variableName);

    /**
     * Number of events routed internally.
     * @return event count routed internally
     */
    public long getRoutedInternal();

    /**
     * Number of events routed externally.
     * @return event count routed externally
     */
    public long getRoutedExternal();

    public void destroy();

    public Map<String, Long> getStatementNearestSchedules();
}
