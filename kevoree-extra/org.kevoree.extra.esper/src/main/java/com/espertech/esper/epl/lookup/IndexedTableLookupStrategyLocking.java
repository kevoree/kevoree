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

import java.util.*;

/**
 * Index lookup strategy for subqueries.
 */
public class IndexedTableLookupStrategyLocking implements TableLookupStrategy
{
    private final TableLookupStrategy inner;
    private final StatementLock statementLock;

    public IndexedTableLookupStrategyLocking(TableLookupStrategy inner, StatementLock statementLock) {
        this.inner = inner;
        this.statementLock = statementLock;
    }

    @Override
    public Collection<EventBean> lookup(EventBean[] events) {
        statementLock.acquireReadLock();
        try {
            Collection<EventBean> result = inner.lookup(events);
            if (result != null) {
                return new ArrayDeque<EventBean>(result);
            }
            else {
                return Collections.emptyList();
            }
        }
        finally {
            statementLock.releaseReadLock();
        }
    }
}
