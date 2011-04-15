/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.timer;

/**
 * Allow for different strategies for getting VM (wall clock) time.
 * See JIRA issue ESPER-191 Support nano/microsecond resolution for more
 * information on Java system time-call performance, accuracy and drift.
 */
public interface TimeSourceService
{
    /**
     * Returns time in millis.
     * @return time in millis
     */
    public long getTimeMillis();
}
