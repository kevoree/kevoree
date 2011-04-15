package com.espertech.esper.epl.named;

/**
 * Observer named window events.
 */
public interface NamedWindowLifecycleObserver
{
    /**
     * Observer named window changes.
     * @param event indicates named window action
     */
    public void observe(NamedWindowLifecycleEvent event);
}
