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
 * Exception to indicate that a property name used in a filter doesn't resolve.
 */
public class PropertyNotFoundException extends StreamTypesException
{
    private static final long serialVersionUID = -29171552032256573L;

    /**
     * Ctor.
     * @param msg - message
     * @param nearestMatchSuggestion - optional suggestion for a matching name
     */
    public PropertyNotFoundException(String msg, Pair<Integer, String> nearestMatchSuggestion)
    {
        super(msg, nearestMatchSuggestion);
    }
}
