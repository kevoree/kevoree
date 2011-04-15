/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.metric;

/**
 * Interface for the time of the metrics generation.
 */
public interface MetricTimeSource
{
    /**
     * Returns current time for metrics reporting.
     * @return metrics current time
     */
    public long getCurrentTime();
}
