/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.exec;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.join.rep.Cursor;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.Set;

/**
 * Strategy for looking up, in some sort of table or index, an event, potentially based on the
 * events properties, and returning a set of matched events.
 */
public interface TableLookupStrategy
{
    /**
     * Returns matched events for a event to look up for. Never returns an empty result set,
     * always returns null to indicate no results.
     * @param event to look up
     * @param cursor the path in the query that the lookup took
     * @param exprEvaluatorContext expression evaluation context
     * @return set of matching events, or null if none matching
     */
    public Set<EventBean> lookup(EventBean event, Cursor cursor, ExprEvaluatorContext exprEvaluatorContext);
}
