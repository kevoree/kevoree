/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.plan;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

/**
 * Property lists stored as a value for each stream-to-stream relationship, for use by {@link QueryGraph}.
 */
public class QueryGraphValue
{
    private List<String> propertiesLeft;
    private List<String> propertiesRight;

    /**
     * Ctor.
     */
    public QueryGraphValue()
    {
        propertiesLeft = new LinkedList<String>();
        propertiesRight = new LinkedList<String>();
    }

    /**
     * Add key and index property.
     * @param keyProperty - key property
     * @param indexProperty - index property
     * @return true if added and either property did not exist, false if either already existed
     */
    public boolean add(String keyProperty, String indexProperty)
    {
        if (propertiesLeft.contains(keyProperty))
        {
            return false;
        }
        if (propertiesRight.contains(indexProperty))
        {
            return false;
        }
        propertiesLeft.add(keyProperty);
        propertiesRight.add(indexProperty);
        return true;
    }

    /**
     * Returns property names for left stream.
     * @return property names
     */
    public List<String> getPropertiesLeft()
    {
        return propertiesLeft;
    }

    /**
     * Returns property names for right stream.
     * @return property names
     */
    public List<String> getPropertiesRight()
    {
        return propertiesRight;
    }

    public String toString()
    {
        return "QueryGraphValue " +
                " propertiesLeft=" + Arrays.toString(propertiesLeft.toArray()) +
                " propertiesRight=" + Arrays.toString(propertiesRight.toArray());
    }
}

