/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern.guard;

import com.espertech.esper.pattern.PatternContext;

/**
 * Receiver for quit events for use by guards.
 */
public interface Quitable
{
    /**
     * Indicate guard quitted.
     */
    public void guardQuit();

    public PatternContext getContext();
}
