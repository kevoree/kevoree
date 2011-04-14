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
 * For use in select clauses for specifying a selected stream: select a.* from MyEvent as a, MyOther as b
 */
public class SelectExprElementStreamRawSpec implements MetaDefItem, Serializable
{
    private String streamName;
    private String optionalAsName;
    private static final long serialVersionUID = -662301837590211453L;

    /**
     * Ctor.
     * @param streamName is the stream name of the stream to select
     * @param optionalAsName is the column name
     */
    public SelectExprElementStreamRawSpec(String streamName, String optionalAsName)
    {
        this.streamName = streamName;
        this.optionalAsName = optionalAsName;
    }

    /**
     * Returns the stream name (e.g. select streamName from MyEvent as streamName).
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
    public String getOptionalAsName()
    {
        return optionalAsName;
    }
}
