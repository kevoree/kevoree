/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join;

import com.espertech.esper.epl.join.table.EventTable;
import com.espertech.esper.epl.join.table.UnindexedEventTableList;
import com.espertech.esper.client.EventBean;

import java.util.List;

/**
 * Strategy of indexing that simply builds an unindexed table of poll results.
 * <p>
 * For use when caching is disabled or when no proper index could be build because no where-clause or on-clause exists or
 * these clauses don't yield indexable columns on analysis.
 */
public class PollResultIndexingStrategyNoIndex implements PollResultIndexingStrategy
{
    public EventTable index(List<EventBean> pollResult, boolean isActiveCache)
    {
        return new UnindexedEventTableList(pollResult);
    }
}
