/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interface for a root state node accepting a callback to use to indicate pattern results.
 */
public interface EvalRootState extends PatternStopCallback
{
    /**
     * Accept callback to indicate pattern results.
     * @param callback is a pattern result call
     */
    public void setCallback(PatternMatchCallback callback);
}
