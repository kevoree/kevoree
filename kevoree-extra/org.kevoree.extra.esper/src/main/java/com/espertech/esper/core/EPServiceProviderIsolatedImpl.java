package com.espertech.esper.core;

import com.espertech.esper.client.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Implementation of the isolated service provider.
 */
public class EPServiceProviderIsolatedImpl implements EPServiceProviderIsolatedSPI
{
    private static final Log log = LogFactory.getLog(EPRuntimeImpl.class);

    private final String name;
    private final EPRuntimeIsolatedImpl runtime;
    private final EPAdministratorIsolatedImpl admin;
    private final EPIsolationUnitServices isolatedServices;
    private final Map<String, EPServiceProviderIsolatedImpl> providers;

    /**
     * Ctor.
     * @param name name of isolated service
     * @param isolatedServices filter and scheduling service isolated
     * @param unisolatedSvc engine services
     * @param providers names and isolated service providers
     */
    public EPServiceProviderIsolatedImpl(String name,
                                         EPIsolationUnitServices isolatedServices,
                                         EPServicesContext unisolatedSvc,
                                         Map<String, EPServiceProviderIsolatedImpl> providers)
    {
        this.name = name;
        this.providers = providers;
        this.isolatedServices = isolatedServices;

        runtime = new EPRuntimeIsolatedImpl(isolatedServices, unisolatedSvc);
        admin = new EPAdministratorIsolatedImpl(name, isolatedServices, unisolatedSvc, runtime);
    }

    public EPIsolationUnitServices getIsolatedServices()
    {
        return isolatedServices;
    }

    public EPRuntimeIsolated getEPRuntime()
    {
        return runtime;
    }

    public EPAdministratorIsolated getEPAdministrator()
    {
        return admin;
    }

    public String getName()
    {
        return name;
    }

    public void destroy()
    {
        providers.remove(name);

        admin.removeAllStatements();
    }
}
