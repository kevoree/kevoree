/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event;

import com.espertech.esper.client.EventSender;
import com.espertech.esper.client.EPException;
import com.espertech.esper.plugin.PlugInEventBeanFactory;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.core.EPRuntimeImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;
import java.util.List;

/**
 * Descriptor for URI-based event sender for plug-in event representations.
 */
public class EventSenderURIDesc
{
    private final PlugInEventBeanFactory beanFactory;
    private final URI resolutionURI;
    private final URI representationURI;

    /**
     * Ctor.
     * @param beanFactory factory for events
     * @param resolutionURI URI use for resolution
     * @param representationURI URI of event representation
     */
    public EventSenderURIDesc(PlugInEventBeanFactory beanFactory, URI resolutionURI, URI representationURI)
    {
        this.beanFactory = beanFactory;
        this.resolutionURI = resolutionURI;
        this.representationURI = representationURI;
    }

    /**
     * URI used for resolution.
     * @return resolution URI
     */
    public URI getResolutionURI()
    {
        return resolutionURI;
    }

    /**
     * URI of event representation.
     * @return URI
     */
    public URI getRepresentationURI()
    {
        return representationURI;
    }

    /**
     * Event wrapper for event objects.
     * @return factory for events
     */
    public PlugInEventBeanFactory getBeanFactory()
    {
        return beanFactory;
    }
}
