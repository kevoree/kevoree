/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.ConfigurationInformation;

/**
 * Interface for a factory class to provide services in a services context for an engine instance.
 */
public interface EPServicesContextFactory
{
    /**
     * Factory method for a new set of engine services.
     * @param epServiceProvider is the engine instance
     * @param configurationSnapshot is a snapshot of configs at the time of engine creation
     * @return services context
     */
    public EPServicesContext createServicesContext(EPServiceProvider epServiceProvider, ConfigurationInformation configurationSnapshot);    
}
