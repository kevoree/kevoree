/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Utility for handling collection or array tasks.
 */
public class CollectionUtil<T>
{
    /**
     * Returns an array of integer values from the set of integer values
     * @param set to return array for
     * @return array
     */
    public static int[] intArray(Set<Integer> set)
    {
        if (set == null)
        {
            return new int[0];
        }
        int[] result = new int[set.size()];
        int index = 0;
        for (Integer value : set) {
            result[index++] = value;
        }
        return result;
    }

    /**
     * Returns a list of the elements invoking toString on non-null elements.
     * @param set to render
     * @param <T> type
     * @return comma-separate list of values (no escape)
     */
    public static <T> String toString(Set<T> set)
    {
        if (set == null)
        {
            return "null";
        }
        if (set.isEmpty())
        {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        String delimiter = "";
        for (T t : set)
        {
            if (t == null)
            {
                continue;
            }
            buf.append(delimiter);
            buf.append(t);
            delimiter = ", ";
        }
        return buf.toString();
    }
}