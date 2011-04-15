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
 * Parameter supplying a single int value is a set of numbers.
 */
public class IntParameter implements NumberSetParameter
{
    private int intValue;
    private static final long serialVersionUID = -895750000874644640L;

    /**
     * Ctor.
     * @param intValue - single in value
     */
    public IntParameter(int intValue)
    {
        this.intValue = intValue;
    }

    /**
     * Returns int value.
     * @return int value
     */
    public int getIntValue()
    {
        return intValue;
    }

    public boolean isWildcard(int min, int max)
    {
        if ((intValue == min) && (intValue == max))
        {
            return true;
        }
        return false;
    }

    public Set<Integer> getValuesInRange(int min, int max)
    {
        Set<Integer> values = new HashSet<Integer>();

        if ((intValue >= min) && (intValue <= max))
        {
            values.add(intValue);
        }

        return values;
    }
}
