/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.type;

import java.util.Set;
import java.util.HashSet;
import java.io.StringWriter;

/**
 * Represents a range of numbers as a parameter.
 */
public class RangeParameter implements NumberSetParameter
{
    private int low;
    private int high;
    private static final long serialVersionUID = 8495531153029613902L;

    /**
     * Ctor.
     * @param low - start of range
     * @param high - end of range
     */
    public RangeParameter(int low, int high)
    {
        this.low = low;
        this.high = high;
    }

    /**
     * Returns start of range.
     * @return start of range
     */
    public int getLow()
    {
        return low;
    }

    /**
     * Returns end of range.
     * @return end of range
     */
    public int getHigh()
    {
        return high;
    }

    public boolean isWildcard(int min, int max)
    {
        if ((min <= low) && (max >= high))
        {
            return true;
        }
        return false;
    }

    public Set<Integer> getValuesInRange(int min, int max)
    {
        Set<Integer> values = new HashSet<Integer>();

        int start = (min > low) ? min : low;
        int end = (max > high) ? high : max;

        while (start <= end)
        {
            values.add(start);
            start++;
        }

        return values;
    }
}
