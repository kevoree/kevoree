/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.plan;

import com.espertech.esper.collection.MultiKey;

import java.util.*;

/**
 * Build query index plans.
 */
public class QueryPlanIndexBuilder
{
    /**
     * Build index specification from navigability info.
     * <p>
     * Looks at each stream and determines which properties in the stream must be indexed
     * in order for other streams to look up into the stream. Determines the unique set of properties
     * to avoid building duplicate indexes on the same set of properties.
     * @param queryGraph - navigability info
     * @return query index specs for each stream
     */
    public static QueryPlanIndex[] buildIndexSpec(QueryGraph queryGraph)
    {
        int numStreams = queryGraph.getNumStreams();
        QueryPlanIndex[] indexSpecs = new QueryPlanIndex[numStreams];

        // For each stream compile a list of index property sets.
        for (int streamIndexed = 0; streamIndexed < numStreams; streamIndexed++)
        {
            Set<MultiKey<String>> indexesSetSortedProps = new HashSet<MultiKey<String>>();
            List<String[]> indexesList = new LinkedList<String[]>();

            // Look at the index from the viewpoint of the stream looking up in the index
            for (int streamLookup = 0; streamLookup < numStreams; streamLookup++)
            {
                if (streamIndexed == streamLookup)
                {
                    continue;
                }

                // Sort index properties, but use the sorted properties only to eliminate duplicates
                String[] indexProps = queryGraph.getIndexProperties(streamLookup, streamIndexed);

                if (indexProps != null)
                {
                    String[] indexPropsSorted = new String[indexProps.length];
                    System.arraycopy(indexProps, 0, indexPropsSorted, 0, indexProps.length);
                    Arrays.sort(indexPropsSorted);

                    // Eliminate duplicates by sorting and using a set
                    MultiKey<String> indexPropsUniqueKey = new MultiKey<String>(indexPropsSorted);
                    if (!indexesSetSortedProps.contains(indexPropsUniqueKey))
                    {
                        indexesSetSortedProps.add(indexPropsUniqueKey);
                        indexesList.add(indexProps);
                    }
                }
            }

            // Copy the index properties for the stream to a QueryPlanIndex instance
            String[][] indexProps;
            if (!indexesList.isEmpty())
            {
                indexProps = new String[indexesList.size()][];
                int count = 0;
                for (String[] entry : indexesList)
                {
                    indexProps[count] = entry;
                    count++;
                }
            }
            else
            {
                // There are no indexes, create a default table for the event set
                indexProps = new String[1][0];
                indexProps[0] = new String[0];
            }
            indexSpecs[streamIndexed] = new QueryPlanIndex(indexProps, new Class[indexProps.length][]);
        }

        return indexSpecs;
    }
}
