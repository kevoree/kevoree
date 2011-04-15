/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.rowregex;

/**
 * Getter that provides an index at runtime.
 */
public class RegexPartitionStateRandomAccessGetter
{
    private final int[] randomAccessIndexesRequested;
    private final int maxPriorIndex;
    private final boolean isUnbound;

    private RegexPartitionStateRandomAccess randomAccess;

    /**
     * Ctor.
     * @param randomAccessIndexesRequested requested indexes
     * @param isUnbound true if unbound
     */
    public RegexPartitionStateRandomAccessGetter(int[] randomAccessIndexesRequested, boolean isUnbound)
    {
        this.randomAccessIndexesRequested = randomAccessIndexesRequested;
        this.isUnbound = isUnbound;

        // Determine the maximum prior index to retain
        int maxPriorIndex = 0;
        for (Integer priorIndex : randomAccessIndexesRequested)
        {
            if (priorIndex > maxPriorIndex)
            {
                maxPriorIndex = priorIndex;
            }
        }
        this.maxPriorIndex = maxPriorIndex;
    }

    /**
     * Returns max index.
     * @return index
     */
    public int getMaxPriorIndex()
    {
        return maxPriorIndex;
    }

    /**
     * Returns indexs.
     * @return indexes.
     */
    public int[] getIndexesRequested()
    {
        return randomAccessIndexesRequested;
    }

    /**
     * Returns length of indexes.
     * @return index len
     */
    public int getIndexesRequestedLen()
    {
        return randomAccessIndexesRequested.length;
    }

    /**
     * Returns true for unbound.
     * @return unbound indicator
     */
    public boolean isUnbound()
    {
        return isUnbound;
    }

    /**
     * Returns the index for access.
     * @return index
     */
    public RegexPartitionStateRandomAccess getAccessor()
    {
        return randomAccess;
    }

    /**
     * Sets the random access.
     * @param randomAccess to use
     */
    public void setRandomAccess(RegexPartitionStateRandomAccess randomAccess)
    {
        this.randomAccess = randomAccess;
    }
}