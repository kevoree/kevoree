/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPRuntimeIsolated;

import java.util.Map;

public interface EPRuntimeIsolatedSPI extends EPRuntimeIsolated
{
    public Map<String, Long> getStatementNearestSchedules();
}
