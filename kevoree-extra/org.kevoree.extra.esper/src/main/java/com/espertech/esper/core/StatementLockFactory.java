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
 * Factory for the managed lock that provides statement resource protection.
 */
public interface StatementLockFactory
{
    /**
     * Create lock for statement
     * @param statementName is the statement name
     * @param expressionText is the statement expression text
     * @return lock
     */
    public StatementLock getStatementLock(String statementName, String expressionText);
}
