/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.db;

import com.espertech.esper.client.ConfigurationCacheReferenceType;
import com.espertech.esper.collection.MultiKey;
import com.espertech.esper.collection.apachecommons.ReferenceMap;
import com.espertech.esper.core.EPStatementHandle;
import com.espertech.esper.core.EPStatementHandleCallback;
import com.espertech.esper.core.ExtensionServicesContext;
import com.espertech.esper.epl.join.table.EventTable;
import com.espertech.esper.schedule.ScheduleHandleCallback;
import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esper.schedule.SchedulingService;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashMap;

/**
 * Implements an expiry-time cache that evicts data when data becomes stale
 * after a given number of seconds.
 * <p>
 * The cache reference type indicates which backing Map is used: Weak type uses the WeakHashMap,
 * Soft type uses the apache commons ReferenceMap, and Hard type simply uses a HashMap.
 */
public class DataCacheExpiringImpl implements DataCache, ScheduleHandleCallback
{
    private final long maxAgeMSec;
    private final long purgeIntervalMSec;
    private final SchedulingService schedulingService;
    private final ScheduleSlot scheduleSlot;
    private final Map<MultiKey<Object>, Item> cache;
    private final EPStatementHandle epStatementHandle;

    private boolean isScheduled;

    /**
     * Ctor.
     * @param maxAgeSec is the maximum age in seconds
     * @param purgeIntervalSec is the purge interval in seconds
     * @param cacheReferenceType indicates whether hard, soft or weak references are used in the cache
     * @param schedulingService is a service for call backs at a scheduled time, for purging
     * @param scheduleSlot slot for scheduling callbacks for this cache
     * @param epStatementHandle is the statements-own handle for use in registering callbacks with services
     */
    public DataCacheExpiringImpl(double maxAgeSec,
                                 double purgeIntervalSec,
                                 ConfigurationCacheReferenceType cacheReferenceType,
                                 SchedulingService schedulingService,
                                 ScheduleSlot scheduleSlot,
                                 EPStatementHandle epStatementHandle)
    {
        this.maxAgeMSec = (long) maxAgeSec * 1000;
        this.purgeIntervalMSec = (long) purgeIntervalSec * 1000;
        this.schedulingService = schedulingService;
        this.scheduleSlot = scheduleSlot;

        if (cacheReferenceType == ConfigurationCacheReferenceType.HARD)
        {
            this.cache = new HashMap<MultiKey<Object>, Item>();
        }
        else if (cacheReferenceType == ConfigurationCacheReferenceType.SOFT)
        {
            this.cache = new ReferenceMap(ReferenceMap.SOFT, ReferenceMap.SOFT);
        }
        else
        {
            this.cache = new WeakHashMap<MultiKey<Object>, Item>();
        }

        this.epStatementHandle = epStatementHandle;
    }

    public EventTable getCached(Object[] lookupKeys)
    {
        MultiKey key = new MultiKey<Object>(lookupKeys);
        Item item = cache.get(key);
        if (item == null)
        {
            return null;
        }

        long now = schedulingService.getTime();
        if ((now - item.getTime()) > maxAgeMSec)
        {
            cache.remove(key);
            return null;
        }

        return item.getData();
    }

    public void put(Object[] lookupKeys, EventTable rows)
    {
        MultiKey key = new MultiKey<Object>(lookupKeys);
        long now = schedulingService.getTime();
        Item item = new Item(rows, now);
        cache.put(key, item);

        if (!isScheduled)
        {
            EPStatementHandleCallback callback = new EPStatementHandleCallback(epStatementHandle, this);
            schedulingService.add(purgeIntervalMSec, callback, scheduleSlot);
            isScheduled = true;
        }
    }

    /**
     * Returns the maximum age in milliseconds.
     * @return millisecon max age
     */
    protected long getMaxAgeMSec()
    {
        return maxAgeMSec;
    }

    /**
     * Returns the purge interval in milliseconds.
     * @return millisecond purge interval
     */
    protected long getPurgeIntervalMSec()
    {
        return purgeIntervalMSec;
    }

    public boolean isActive()
    {
        return true;
    }

    /**
     * Returns the current cache size.
     * @return cache size
     */
    protected long getSize()
    {
        return cache.size();
    }

    public void scheduledTrigger(ExtensionServicesContext extensionServicesContext)
    {
        // purge expired
        long now = schedulingService.getTime();
        Iterator<MultiKey<Object>> it = cache.keySet().iterator();
        for (;it.hasNext();)
        {
            Item item = cache.get(it.next());
            if ((now - item.getTime()) > maxAgeMSec)
            {
                it.remove();
            }
        }

        isScheduled = false;
    }

    private static class Item
    {
        private EventTable data;
        private long time;

        public Item(EventTable data, long time)
        {
            this.data = data;
            this.time = time;
        }

        public EventTable getData()
        {
            return data;
        }

        public long getTime()
        {
            return time;
        }
    }
}
