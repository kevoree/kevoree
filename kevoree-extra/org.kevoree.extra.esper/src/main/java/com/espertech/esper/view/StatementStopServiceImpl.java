/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view;

import java.util.List;
import java.util.LinkedList;

/**
 * Provides subscription list for statement stop callbacks.
 */
public class StatementStopServiceImpl implements StatementStopService
{
    private List<StatementStopCallback> statementStopCallbacks;

    /**
     * ctor.
     */
    public StatementStopServiceImpl()
    {
        statementStopCallbacks = new LinkedList<StatementStopCallback>();
    }

    public void addSubscriber(StatementStopCallback callback)
    {
        statementStopCallbacks.add(callback);
    }

    public void fireStatementStopped()
    {
        for (StatementStopCallback statementStopCallback : statementStopCallbacks)
        {
            statementStopCallback.statementStopped();
        }
    }
}
