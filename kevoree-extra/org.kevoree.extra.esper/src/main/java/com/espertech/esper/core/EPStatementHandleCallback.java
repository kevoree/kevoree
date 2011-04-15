/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.filter.FilterHandle;
import com.espertech.esper.filter.FilterHandleCallback;
import com.espertech.esper.schedule.ScheduleHandle;
import com.espertech.esper.schedule.ScheduleHandleCallback;

/**
 * Statement resource handle and callback for use with {@link com.espertech.esper.filter.FilterService} and
 * {@link com.espertech.esper.schedule.SchedulingService}.
 * <p>
 * Links the statement handle identifying a statement and containing the statement resource lock,
 * with the actual callback to invoke for a statement together.
 */
public class EPStatementHandleCallback implements FilterHandle, ScheduleHandle
{
    private EPStatementHandle epStatementHandle;
    private FilterHandleCallback filterCallback;
    private ScheduleHandleCallback scheduleCallback;

    /**
     * Ctor.
     * @param epStatementHandle is a statement handle
     * @param callback is a filter callback
     */
    public EPStatementHandleCallback(EPStatementHandle epStatementHandle, FilterHandleCallback callback)
    {
        this.epStatementHandle = epStatementHandle;
        this.filterCallback = callback;
    }

    /**
     * Ctor.
     * @param epStatementHandle is a statement handle
     * @param callback is a schedule callback
     */
    public EPStatementHandleCallback(EPStatementHandle epStatementHandle, ScheduleHandleCallback callback)
    {
        this.epStatementHandle = epStatementHandle;
        this.scheduleCallback = callback;
    }

    public String getStatementId()
    {
        return epStatementHandle.getStatementId();
    }

    /**
     * Returns the statement handle.
     * @return handle containing a statement resource lock
     */
    public EPStatementHandle getEpStatementHandle()
    {
        return epStatementHandle;
    }

    /**
     * Returns the statement filter callback, or null if this is a schedule callback handle.
     * @return filter callback
     */
    public FilterHandleCallback getFilterCallback()
    {
        return filterCallback;
    }

    /**
     * Returns the statement schedule callback, or null if this is a filter callback handle.
     * @return schedule callback
     */
    public ScheduleHandleCallback getScheduleCallback()
    {
        return scheduleCallback;
    }
}
