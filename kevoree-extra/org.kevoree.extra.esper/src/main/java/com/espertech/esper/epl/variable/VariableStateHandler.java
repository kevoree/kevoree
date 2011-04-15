/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.variable;

import com.espertech.esper.core.StatementExtensionSvcContext;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.client.EventType;

/**
 * Interface for a plug-in to {@link VariableService} to handle variable persistent state.
 */
public interface VariableStateHandler
{
    /**
     * Returns the current variable state plus Boolean.TRUE if there is a current state since the variable
     * may have the value of null; returns Boolean.FALSE and null if there is no current state
     * @param variableName variable name
     * @param variableNumber number of the variable
     * @param type type of the variable
     * @param eventType event type or null if not a variable that represents an event
     * @param statementExtContext for caches etc.
     * @return indicator whether the variable is known and it's state, or whether it doesn't have state (false)
     */
    public Pair<Boolean, Object> getHasState(String variableName, int variableNumber, Class type, EventType eventType, StatementExtensionSvcContext statementExtContext);

    /**
     * Sets the new variable value
     * @param variableName name of the variable
     * @param variableNumber number of the variable
     * @param newValue new variable value, null values allowed
     */
    public void setState(String variableName, int variableNumber, Object newValue);
}
