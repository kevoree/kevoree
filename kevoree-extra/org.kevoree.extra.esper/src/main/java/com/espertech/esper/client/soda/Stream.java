/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import java.io.Serializable;
import java.io.StringWriter;

/**
 * An abstract base class for a named or unnamed stream.
 * <p>
 * Named streams provide an as-name for the stream, for example "select * from MyEvents(id=10) as StreamZero".
 * Unnamed streams provide no as-name for the stream, for example "select * from MyEvents(id=10)".
 */
public abstract class Stream implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String streamName;

    /**
     * Ctor.
     */
    public Stream() {
    }

    /**
     * Renders the stream in textual representation.
     * @param writer to output to
     */
    public abstract void toEPLStream(StringWriter writer);
    
    /**
     * Renders the stream in textual representation any stream options, if present.
     * @param writer to output to
     */
    public abstract void toEPLStreamOptions(StringWriter writer);

    /**
     * Renders the stream type under a non-complete textual representation for tool use
     * @param writer to output to
     */
    public abstract void toEPLStreamType(StringWriter writer);

    /**
     * Ctor.
     * @param streamName is null for unnamed streams, or a stream name for named streams.
     */
    protected Stream(String streamName)
    {
        this.streamName = streamName;
    }

    /**
     * Returns the stream name.
     * @return name of stream, or null if unnamed.
     */
    public String getStreamName()
    {
        return streamName;
    }

    /**
     * Sets the stream name.
     * @param streamName is the name of stream, or null if unnamed.
     */
    public void setStreamName(String streamName)
    {
        this.streamName = streamName;
    }

    /**
     * Renders the clause in textual representation.
     * @param writer to output to
     */
    public void toEPL(StringWriter writer)
    {
        toEPLStream(writer);

        if (streamName != null)
        {
            writer.write(" as ");
            writer.write(streamName);
        }

        toEPLStreamOptions(writer);
    }

}
