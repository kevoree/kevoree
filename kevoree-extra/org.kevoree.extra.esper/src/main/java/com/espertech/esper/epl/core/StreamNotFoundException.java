/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

import com.espertech.esper.collection.Pair;

/**
 * Exception to indicate that a stream name could not be resolved.
 */
public class StreamNotFoundException extends StreamTypesException
{
    private static final long serialVersionUID = -665030219652415977L;

    /**
     * Ctor.
     * @param msg - message
     * @param suggestion - optional suggestion for a matching name
     */
    public StreamNotFoundException(String msg, Pair<Integer, String> suggestion)
    {
        super(msg, suggestion);
    }
}
