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
import com.espertech.esper.epl.join.exec.ExecNode;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

import java.util.Set;
import java.util.List;
import java.util.LinkedList;

/**
 * Query strategy for building a join tuple set by using an execution node tree.
 */
public class ExecNodeQueryStrategy implements QueryStrategy
{
    private int forStream;
    private int numStreams;
    private ExecNode execNode;

    /**
     * CTor.
     * @param forStream - stream the strategy is for
     * @param numStreams - number of streams in total
     * @param execNode - execution node for building join tuple set
     */
    public ExecNodeQueryStrategy(int forStream, int numStreams, ExecNode execNode)
    {
        this.forStream = forStream;
        this.numStreams = numStreams;
        this.execNode = execNode;
    }

    public void lookup(EventBean[] lookupEvents, Set<MultiKey<EventBean>> joinSet, ExprEvaluatorContext exprEvaluatorContext)
    {
        if (lookupEvents == null)
        {
            return;
        }

        for (EventBean event : lookupEvents)
        {
            // Set up prototype row
            EventBean[] prototype = new EventBean[numStreams];
            prototype[forStream] = event;

            // Perform execution
            List<EventBean[]> results = new LinkedList<EventBean[]>();
            execNode.process(event, prototype, results, exprEvaluatorContext);

            // Convert results into unique set
            for (EventBean[] row : results)
            {
                joinSet.add(new MultiKey<EventBean>(row));
            }
        }
    }

    /**
     * Return stream number this strategy is for.
     * @return stream num
     */
    protected int getForStream()
    {
        return forStream;
    }

    /**
     * Returns the total number of streams.
     * @return number of streams
     */
    protected int getNumStreams()
    {
        return numStreams;
    }

    /**
     * Returns execution node.
     * @return execution node
     */
    protected ExecNode getExecNode()
    {
        return execNode;
    }
}
