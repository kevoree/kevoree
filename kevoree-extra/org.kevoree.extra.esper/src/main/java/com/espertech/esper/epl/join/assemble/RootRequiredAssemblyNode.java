/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.assemble;

import com.espertech.esper.epl.join.rep.Node;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.util.IndentWriter;

import java.util.List;

/**
 * Assembly node for an event stream that is a root with a one required child node below it.
 */
public class RootRequiredAssemblyNode extends BaseAssemblyNode
{
    /**
     * Ctor.
     * @param streamNum - is the stream number
     * @param numStreams - is the number of streams
     */
    public RootRequiredAssemblyNode(int streamNum, int numStreams)
    {
        super(streamNum, numStreams);
    }

    public void init(List<Node>[] result)
    {
        // need not be concerned with results, all is passed from the child node
    }

    public void process(List<Node>[] result)
    {
        // no action here, since we have a required child row
        // The single required child generates all events that may exist
    }

    public void result(EventBean[] row, int fromStreamNum, EventBean myEvent, Node myNode)
    {
        parentNode.result(row, streamNum, null, null);
    }

    public void print(IndentWriter indentWriter)
    {
        indentWriter.println("RootRequiredAssemblyNode streamNum=" + streamNum);
    }
}
