/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.stream;

import com.espertech.esper.core.StatementLock;
import com.espertech.esper.view.EventStream;
import com.espertech.esper.filter.FilterSpecCompiled;
import com.espertech.esper.filter.FilterService;
import com.espertech.esper.core.EPStatementHandle;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;

/**
 * Service on top of the filter service for reuseing filter callbacks and their associated EventStream instances.
 * Same filter specifications (equal) do not need to be added to the filter service twice and the
 * EventStream instance that is the stream of events for that filter can be reused.
 * <p>
 * We are re-using streams such that views under such streams can be reused for efficient resource use.
 */
public interface StreamFactoryService
{
    /**
     * Create or reuse existing EventStream instance representing that event filter.
     * When called for some filters, should return same stream.
     * @param filterSpec event filter definition
     * @param filterService filter service to activate filter if not already active
     * @param epStatementHandle is the statements-own handle for use in registering callbacks with services
     * @param isJoin is indicatng whether the stream will participate in a join statement, information
     * necessary for stream reuse and multithreading concerns
     * @param isSubSelect true for subselects
     * @param statementId the statement id
     * @param exprEvaluatorContext expression evaluation context
     * @param isNamedWindowTrigger if a named window or trigger querying from named window
     * @return event stream representing active filter
     */
    public Pair<EventStream, StatementLock> createStream(final String statementId, FilterSpecCompiled filterSpec, FilterService filterService, EPStatementHandle epStatementHandle,
                                    boolean isJoin, boolean isSubSelect, ExprEvaluatorContext exprEvaluatorContext, boolean isNamedWindowTrigger);

    /**
     * Drop the event stream associated with the filter passed in.
     * Throws an exception if already dropped.
     * @param filterSpec is the event filter definition associated with the event stream to be dropped
     * @param filterService to be used to deactivate filter when the last event stream is dropped
     * @param isJoin is indicatng whether the stream will participate in a join statement, information
     * necessary for stream reuse and multithreading concerns
     * @param isSubSelect true for subselects
     * @param isNamedWindowTrigger if a named window or trigger querying from named window
     */
    public void dropStream(FilterSpecCompiled filterSpec, FilterService filterService, boolean isJoin, boolean isSubSelect, boolean isNamedWindowTrigger);

    /**
     * Destroy the service.
     */
    public void destroy();
}
