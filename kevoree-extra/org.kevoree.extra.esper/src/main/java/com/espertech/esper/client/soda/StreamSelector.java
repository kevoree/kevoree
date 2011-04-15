/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

/**
 * Enumeration for representing selection of the remove stream or the insert stream, or both.
 */
public enum StreamSelector
{
    /**
     * Indicates selection of the remove stream only.
     */
    RSTREAM_ONLY,

    /**
     * Indicates selection of the insert stream only.
     */
    ISTREAM_ONLY,

    /**
     * Indicates selection of both the insert and the remove stream.
     */
    RSTREAM_ISTREAM_BOTH
}
