/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.plan;

import com.espertech.esper.collection.UniformPair;

/**
 * Key consisting of 2 integer stream numbers, for use by {@link QueryGraph}.
 */
public class QueryGraphKey
{
    private UniformPair<Integer> streams;

    /**
     * Ctor.
     * @param streamOne - from stream
     * @param streamTwo - to stream
     */
    public QueryGraphKey(int streamOne, int streamTwo)
    {
        if (streamOne > streamTwo)
        {
            int temp = streamTwo;
            streamTwo = streamOne;
            streamOne = temp;
        }
        streams = new UniformPair<Integer>(streamOne, streamTwo);
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof QueryGraphKey))
        {
            return false;
        }

        QueryGraphKey other = (QueryGraphKey) obj;
        return other.streams.equals(this.streams);
    }

    public int hashCode()
    {
        return streams.hashCode();
    }

    public String toString()
    {
        return "QueryGraphKey " + streams.getFirst() + " and " + streams.getSecond();
    }
}

