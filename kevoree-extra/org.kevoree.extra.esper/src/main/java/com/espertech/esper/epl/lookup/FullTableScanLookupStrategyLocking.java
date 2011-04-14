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
import com.espertech.esper.core.StatementLock;

import java.util.ArrayDeque;
import java.util.Collection;

/**
 * Index lookup strategy for subqueries.
 */
public class FullTableScanLookupStrategyLocking implements TableLookupStrategy
{
    private final Iterable<EventBean> contents;
    private final StatementLock statementLock;

    public FullTableScanLookupStrategyLocking(Iterable<EventBean> contents, StatementLock statementLock) {
        this.contents = contents;
        this.statementLock = statementLock;
    }

    @Override
    public Collection<EventBean> lookup(EventBean[] events) {
        statementLock.acquireReadLock();
        try {
            ArrayDeque<EventBean> result = new ArrayDeque<EventBean>();
            for (EventBean eventBean : contents) {
                result.add(eventBean);
            }
            return result;
        }
        finally {
            statementLock.releaseReadLock();
        }
    }
}
