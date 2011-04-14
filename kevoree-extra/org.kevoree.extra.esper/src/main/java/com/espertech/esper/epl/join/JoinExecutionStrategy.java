/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.collection.MultiKey;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.Set;

/**
 * Strategy for executing a join.
 */
public interface JoinExecutionStrategy
{
    /**
     * Execute join. The first dimension in the 2-dim arrays is the stream that generated the events,
     * and the second dimension is the actual events generated.
     * @param newDataPerStream - new events for each stream
     * @param oldDataPerStream - old events for each stream
     * @param exprEvaluatorContext expression evaluation context
     */
    public void join(EventBean[][] newDataPerStream,
                     EventBean[][] oldDataPerStream,
                     ExprEvaluatorContext exprEvaluatorContext);

    /**
     * A static join is for use with iterating over join statements.
     * @return set of rows, each row with two or more events, one for each stream
     */
    public Set<MultiKey<EventBean>> staticJoin();
}