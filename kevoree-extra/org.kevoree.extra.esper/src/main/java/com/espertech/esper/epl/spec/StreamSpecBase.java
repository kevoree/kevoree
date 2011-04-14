/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.epl.spec.ViewSpec;
import com.espertech.esper.util.MetaDefItem;

import java.util.List;
import java.util.LinkedList;
import java.io.Serializable;

/**
 * Abstract base specification for a stream, consists simply of an optional stream name and a list of views
 * on to of the stream.
 * <p>
 * Implementation classes for views and patterns add additional information defining the
 * stream of events.
 */
public abstract class StreamSpecBase implements MetaDefItem, Serializable
{
    private static final long serialVersionUID = 0L;

    private String optionalStreamName;
    private List<ViewSpec> viewSpecs = new LinkedList<ViewSpec>();
    private StreamSpecOptions streamSpecOptions;

    /**
     * Ctor.
     * @param optionalStreamName - stream name, or null if none supplied
     * @param viewSpecs - specifies what view to use to derive data
     * @param streamSpecOptions - indicates additional options such as unidirectional stream or retain-union or retain-intersection
     */
    public StreamSpecBase(String optionalStreamName, List<ViewSpec> viewSpecs, StreamSpecOptions streamSpecOptions)
    {
        this.optionalStreamName = optionalStreamName;
        this.viewSpecs.addAll(viewSpecs);
        this.streamSpecOptions = streamSpecOptions;
    }

    /**
     * Default ctor.
     */
    public StreamSpecBase()
    {
    }

    /**
     * Returns the name assigned.
     * @return stream name or null if not assigned
     */
    public String getOptionalStreamName()
    {
        return optionalStreamName;
    }

    /**
     * Returns view definitions to use to construct views to derive data on stream.
     * @return view defs
     */
    public List<ViewSpec> getViewSpecs()
    {
        return viewSpecs;
    }

    /**
     * Returns the options for the stream such as unidirectional, retain-union etc.
     * @return stream options
     */
    public StreamSpecOptions getOptions()
    {
        return streamSpecOptions;
    }
}
