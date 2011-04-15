/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.db;

import com.espertech.esper.epl.join.table.EventTable;
import com.espertech.esper.collection.MultiKey;

import java.util.Map;
import java.util.HashMap;

/**
 * For use in iteration over historical joins, a {@link DataCache} implementation
 * that serves to hold EventBean rows generated during a join evaluation
 * involving historical streams stable for the same cache lookup keys.
 */
public class DataCacheClearableMap implements DataCache
{
    private Map<MultiKey<Object>, EventTable> cache;

    /**
     * Ctor.
     */
    public DataCacheClearableMap()
    {
        this.cache = new HashMap<MultiKey<Object>, EventTable>();
    }

    public EventTable getCached(Object[] lookupKeys)
    {
        MultiKey<Object> key = new MultiKey<Object>(lookupKeys);
        return cache.get(key);
    }

    public void put(Object[] lookupKeys, EventTable rows)
    {
        MultiKey<Object> key = new MultiKey<Object>(lookupKeys);
        cache.put(key, rows);
    }

    public boolean isActive()
    {
        return false;
    }

    /**
     * Clears the cache.
     */
    public void clear()
    {
        cache.clear();
    }
}
