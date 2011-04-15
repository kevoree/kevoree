package com.espertech.esper.core;

import com.espertech.esper.client.*;

/**
 * SPU for isolated service provider.
 */
public interface EPServiceProviderIsolatedSPI extends EPServiceProviderIsolated
{
    /**
     * Return isolated services.
     * @return isolated services
     */
    public EPIsolationUnitServices getIsolatedServices();
}