/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

/**
 * Provides statement-level locks.
 */
public class StatementLockFactoryImpl implements StatementLockFactory
{
    private final boolean fairlocks;

    public StatementLockFactoryImpl(boolean fairlocks) {
        this.fairlocks = fairlocks;
    }

    public StatementLock getStatementLock(String statementName, String expressionText)
    {
        return new StatementRWLockImpl(statementName, fairlocks);
    }
}
