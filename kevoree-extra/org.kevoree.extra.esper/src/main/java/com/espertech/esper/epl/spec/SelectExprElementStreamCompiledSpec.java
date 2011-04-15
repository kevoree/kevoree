/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.util.MetaDefItem;

import java.io.Serializable;

/**
 * Mirror class to {@link SelectExprElementStreamRawSpec} but added the stream number for the name.
 */
public class SelectExprElementStreamCompiledSpec implements MetaDefItem, Serializable
{
    private final String streamName;
    private final String optionalName;
    private final int streamNumber;
    private final boolean isTaggedEvent;
    private static final long serialVersionUID = 1220770708564056643L;

    /**
     * Ctor.
     * @param streamName is the stream name of the stream to select
     * @param optionalColumnName is the column name
     * @param streamNumber is the number of the stream
     * @param isTaggedEvent is true to indicate that we are meaning to select a tagged event in a pattern
     */
    public SelectExprElementStreamCompiledSpec(String streamName, String optionalColumnName, int streamNumber, boolean isTaggedEvent)
    {
        this.streamName = streamName;
        this.optionalName = optionalColumnName;
        this.streamNumber = streamNumber;
        this.isTaggedEvent = isTaggedEvent;
    }

    /**
     * Returns the stream name.
     * @return name
     */
    public String getStreamName()
    {
        return streamName;
    }

    /**
     * Returns the column name.
     * @return name
     */
    public String getOptionalName()
    {
        return optionalName;
    }

    /**
     * Returns the stream number of the stream for the stream name.
     * @return stream number
     */
    public int getStreamNumber()
    {
        return streamNumber;
    }

    /**
     * Returns true to indicate that we are meaning to select a tagged event in a pattern, or false if
     * selecting an event from a stream.
     * @return true for tagged event in pattern, false for stream
     */
    public boolean isTaggedEvent()
    {
        return isTaggedEvent;
    }
}
