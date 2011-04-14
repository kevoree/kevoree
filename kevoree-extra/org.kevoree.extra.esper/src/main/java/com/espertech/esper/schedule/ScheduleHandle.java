/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.schedule;

/**
 * Marker interface for use with {@link SchedulingService}. Implementations serve as a schedule trigger values when
 * the schedule is reached to trigger or return the handle.
 */
public interface ScheduleHandle
{
    /**
     * Returns the statement id.
     * @return statement id
     */
    public String getStatementId();
}
