package com.espertech.esper.client;

import com.espertech.esper.core.EPServiceProviderImpl;

/**
 * A listener interface for callbacks regarding {@link EPServiceProvider} state changes.
 */
public interface EPServiceStateListener
{
    /**
     * Invoked before an {@link EPServiceProvider} is destroyed.
     * @param serviceProvider service provider to be destroyed
     */
    public void onEPServiceDestroyRequested(EPServiceProvider serviceProvider);

    /**
     * Invoked after an existing {@link EPServiceProvider} is initialized upon completion of a call to initialize.
     * @param serviceProvider service provider that has been successfully initialized
     */
    public void onEPServiceInitialized(EPServiceProvider serviceProvider);
}
