package com.espertech.esper.epl.core;

import com.espertech.esper.client.EventType;
import com.espertech.esper.event.EventTypeSPI;

import java.util.HashSet;

/**
 * Registry for event types creates as part of the select expression analysis.
 */
public class SelectExprEventTypeRegistry
{
    private HashSet<String> registry;

    /**
     * Ctor.
     * @param registry the holder of the registry
     */
    public SelectExprEventTypeRegistry(HashSet<String> registry)
    {
        this.registry = registry;
    }

    /**
     * Adds an event type.
     * @param eventType to add
     */
    public void add(EventType eventType)
    {
        if (!(eventType instanceof EventTypeSPI))
        {
            return;
        }
        registry.add(((EventTypeSPI) eventType).getMetadata().getPrimaryName());
    }
}
