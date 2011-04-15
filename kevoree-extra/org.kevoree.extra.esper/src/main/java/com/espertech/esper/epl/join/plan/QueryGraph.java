/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.plan;

import java.util.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Model of relationships between streams based on properties in both streams that are
 * specified as equal in a filter expression.
 */
public class QueryGraph
{
    private final int numStreams;
    private final Map<QueryGraphKey, QueryGraphValue> streamJoinMap;

    /**
     * Ctor.
     * @param numStreams - number of streams
     */
    public QueryGraph(int numStreams)
    {
        this.numStreams = numStreams;
        streamJoinMap = new HashMap<QueryGraphKey, QueryGraphValue>();
    }

    /**
     * Returns the number of streams.
     * @return number of streams
     */
    public int getNumStreams()
    {
        return numStreams;
    }

    /**
     * Add properties for 2 streams that are equal.
     * @param streamLeft - left hand stream
     * @param propertyLeft - left hand stream property
     * @param streamRight - right hand stream
     * @param propertyRight - right hand stream property
     * @return true if added and did not exist, false if already known
     */
    public boolean add(int streamLeft, String propertyLeft, int streamRight, String propertyRight)
    {
        if (propertyLeft == null || propertyRight == null)
        {
            throw new IllegalArgumentException("Null property names supplied");
        }

        if (streamLeft == streamRight)
        {
            throw new IllegalArgumentException("Streams supplied are the same");
        }

        QueryGraphKey key = new QueryGraphKey(streamLeft, streamRight);
        QueryGraphValue value = streamJoinMap.get(key);

        if (value == null)
        {
            value = new QueryGraphValue();
            streamJoinMap.put(key, value);
        }

        if (streamLeft > streamRight)
        {
            return value.add(propertyRight, propertyLeft);
        }
        else
        {
            return value.add(propertyLeft, propertyRight);
        }
    }

    /**
     * Returns true if there is a relationship between streams via equal properties.
     * @param streamFrom - from stream number
     * @param streamTo - to stream number
     * @return true if relationship exists, false if not
     */
    public boolean isNavigable(int streamFrom, int streamTo)
    {
        QueryGraphKey key = new QueryGraphKey(streamFrom, streamTo);
        return streamJoinMap.containsKey(key);
    }

    /**
     * Returns set of streams that the given stream is navigable to.
     * @param streamFrom - from stream number
     * @return set of streams related to this stream, or empty set if none
     */
    public Set<Integer> getNavigableStreams(int streamFrom)
    {
        Set<Integer> result = new HashSet<Integer>();
        for (int i = 0; i < numStreams; i++)
        {
            if (isNavigable(streamFrom, i))
            {
                result.add(i);
            }
        }
        return result;
    }

    /**
     * Returns index properties.
     * @param streamLookup - stream to serve as source for looking up events
     * @param streamIndexed - stream to look up in
     * @return index property names
     */
    public String[] getIndexProperties(int streamLookup, int streamIndexed)
    {
        QueryGraphKey key = new QueryGraphKey(streamLookup, streamIndexed);
        QueryGraphValue value = streamJoinMap.get(key);

        if (value == null)
        {
            return null;
        }

        if (streamLookup > streamIndexed)
        {
            return value.getPropertiesLeft().toArray(new String[value.getPropertiesLeft().size()]);
        }
        return value.getPropertiesRight().toArray(new String[value.getPropertiesRight().size()]);
    }

    /**
     * Returns key properties.
     * @param streamLookup - stream to serve as source for looking up events
     * @param streamIndexed - stream to look up in
     * @return key property names
     */
    public String[] getKeyProperties(int streamLookup, int streamIndexed)
    {
        QueryGraphKey key = new QueryGraphKey(streamLookup, streamIndexed);
        QueryGraphValue value = streamJoinMap.get(key);

        if (value == null)
        {
            return null;
        }

        if (streamLookup > streamIndexed)
        {
            return value.getPropertiesRight().toArray(new String[value.getPropertiesRight().size()]);
        }
        return value.getPropertiesLeft().toArray(new String[value.getPropertiesLeft().size()]);
    }

    /**
     * Fill in equivalent key properties (navigation entries) on all streams.
     * For example, if  a=b and b=c  then add a=c. The method adds new equalivalent key properties
     * until no additional entries to be added are found, ie. several passes can be made.
     * @param queryGraph - navigablity info between streamss
     */
    public static void fillEquivalentNav(QueryGraph queryGraph)
    {
        boolean addedEquivalency;

        // Repeat until no more entries were added
        do
        {
            addedEquivalency = false;

            // For each stream-to-stream combination
            for (int lookupStream = 0; lookupStream < queryGraph.numStreams; lookupStream++)
            {
                for (int indexedStream = 0; indexedStream < queryGraph.numStreams; indexedStream++)
                {
                    if (lookupStream == indexedStream)
                    {
                        continue;
                    }

                    boolean added = fillEquivalentNav(queryGraph, lookupStream, indexedStream);
                    if (added)
                    {
                        addedEquivalency = true;
                    }
                }
            }
        }
        while (addedEquivalency);
    }

    /*
     * Looks at the key and index (aka. left and right) properties of the 2 streams and checks
     * for each property if any equivalent index properties exist for other streams.
     */
    private static boolean fillEquivalentNav(QueryGraph queryGraph, int lookupStream, int indexedStream)
    {
        boolean addedEquivalency = false;
        String[] keyProps = queryGraph.getKeyProperties(lookupStream, indexedStream);
        String[] indexProps = queryGraph.getIndexProperties(lookupStream, indexedStream);

        if (keyProps == null)
        {
            return false;
        }
        if (keyProps.length != indexProps.length)
        {
            throw new IllegalStateException("Unexpected key and index property number mismatch");
        }

        for (int i = 0; i < keyProps.length; i++)
        {
            boolean added = fillEquivalentNav(queryGraph, lookupStream, keyProps[i], indexedStream, indexProps[i]);
            if (added)
            {
                addedEquivalency = true;
            }
        }

        return addedEquivalency;
    }

    /*
     * Looks at the key and index (aka. left and right) properties of the 2 streams and checks
     * for each property if any equivalent index properties exist for other streams.
     *
     * Example:  s0.p0 = s1.p1  and  s1.p1 = s2.p2  ==> therefore s0.p0 = s2.p2
     * ==> look stream s0, property p0; indexed stream s1, property p1
     * Is there any other lookup stream that has stream 1 and property p1 as index property? ==> this is stream s2, p2
     * Add navigation entry between stream s0 and property p0 to stream s2, property p2
     */
    private static boolean fillEquivalentNav(QueryGraph queryGraph, int lookupStream, String keyProp, int indexedStream, String indexProp)
    {
        boolean addedEquivalency = false;

        for (int otherStream = 0; otherStream < queryGraph.numStreams; otherStream++)
        {
            if ((otherStream == lookupStream) || (otherStream == indexedStream))
            {
                continue;
            }

            String[] otherKeyProps = queryGraph.getKeyProperties(otherStream, indexedStream);
            String[] otherIndexProps = queryGraph.getIndexProperties(otherStream, indexedStream);
            int otherPropertyNum = -1;

            if (otherIndexProps == null)
            {
                continue;
            }

            for (int i = 0; i < otherIndexProps.length; i++)
            {
                if (otherIndexProps[i].equals(indexProp))
                {
                    otherPropertyNum = i;
                    break;
                }
            }

            if (otherPropertyNum != -1)
            {
                boolean added = queryGraph.add(lookupStream, keyProp, otherStream, otherKeyProps[otherPropertyNum]);
                if (added)
                {
                    addedEquivalency = true;
                }
            }
        }

        return addedEquivalency;
    }

    public String toString()
    {
        StringWriter buf = new StringWriter();
        PrintWriter writer = new PrintWriter(buf);

        int count = 0;
        for (Map.Entry<QueryGraphKey, QueryGraphValue> entry : streamJoinMap.entrySet())
        {
            count++;
            writer.println("Entry " + count + ": key=" + entry.getKey());
            writer.println("  value=" + entry.getValue());
        }

        return buf.toString();
    }
}
