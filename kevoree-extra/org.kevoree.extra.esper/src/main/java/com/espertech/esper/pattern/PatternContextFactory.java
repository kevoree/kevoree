/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import com.espertech.esper.core.StatementContext;

/**
 * Factory for pattern context instances, creating context objects for each distinct pattern based on the
 * patterns root node and stream id.
 */
public interface PatternContextFactory
{
    /**
     * Create a pattern context.
     * @param statementContext is the statement information and services
     * @param streamId is the stream id
     * @param rootNode is the pattern root node
     * @param hasArrayProperties is the pattern has array properties
     * @return pattern context
     */
    public PatternContext createContext(StatementContext statementContext,
                                        int streamId,
                                        EvalRootNode rootNode,
                                        boolean hasArrayProperties);
}
