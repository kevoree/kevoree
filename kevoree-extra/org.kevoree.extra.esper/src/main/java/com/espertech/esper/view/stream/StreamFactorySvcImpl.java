/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.stream;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.collection.RefCountedMap;
import com.espertech.esper.core.EPStatementHandle;
import com.espertech.esper.core.EPStatementHandleCallback;
import com.espertech.esper.core.StatementLock;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.filter.FilterHandleCallback;
import com.espertech.esper.filter.FilterService;
import com.espertech.esper.filter.FilterSpecCompiled;
import com.espertech.esper.filter.FilterValueSet;
import com.espertech.esper.view.EventStream;
import com.espertech.esper.view.ZeroDepthStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.IdentityHashMap;


/**
 * Service implementation to reuse or not reuse event streams and existing filters depending on
 * the type of statement.
 * <p>
 * For non-join statements, the class manages the reuse of event streams when filters match, and thus
 * when an event stream is reused such can be the views under the stream. For joins however, this can lead to
 * problems in multithread-safety since the statement resource lock would then have to be multiple locks,
 * i.e. the reused statement's resource lock and the join statement's own lock, at a minimum.
 * <p>
 * For join statements, always creating a new event stream and
 * therefore not reusing view resources, for use with joins.
 * <p>
 * This can be very effective in that if a client applications creates a large number of very similar
 * statements in terms of filters and views used then these resources are all re-used
 * across statements.
 * <p>
 * The re-use is multithread-safe in that
 * (A) statement start/stop is locked against other engine processing
 * (B) the first statement supplies the lock for shared filters and views, protecting multiple threads
 * from entering into the same view.
 * (C) joins statements do not participate in filter and view reuse
 */
public class StreamFactorySvcImpl implements StreamFactoryService
{
    private static Log log = LogFactory.getLog(StreamFactorySvcImpl.class);

    // Using identify hash map - ignoring the equals semantics on filter specs
    // Thus two filter specs objects are always separate entries in the map
    private final IdentityHashMap<FilterSpecCompiled, Pair<EventStream, EPStatementHandleCallback>> eventStreamsIdentity;

    // Using a reference-counted map for non-join statements
    private final RefCountedMap<FilterSpecCompiled, Pair<EventStream, EPStatementHandleCallback>> eventStreamsRefCounted;

    private boolean isReuseViews;

    /**
     * Ctor.
     * @param isReuseViews indicator on whether stream and view resources are to be reused between statements
     */
    public StreamFactorySvcImpl(boolean isReuseViews)
    {
        this.eventStreamsRefCounted = new RefCountedMap<FilterSpecCompiled, Pair<EventStream, EPStatementHandleCallback>>();
        this.eventStreamsIdentity = new IdentityHashMap<FilterSpecCompiled, Pair<EventStream, EPStatementHandleCallback>>();
        this.isReuseViews = isReuseViews;
    }

    public void destroy()
    {
        eventStreamsRefCounted.clear();
        eventStreamsIdentity.clear();
    }

    /**
     * See the method of the same name in {@link com.espertech.esper.view.stream.StreamFactoryService}. Always attempts to reuse an existing event stream.
     * May thus return a new event stream or an existing event stream depending on whether filter criteria match.
     * @param filterSpec is the filter definition
     * @param epStatementHandle is the statement resource lock
     * @return newly createdStatement event stream, not reusing existing instances
     */
    public Pair<EventStream, StatementLock> createStream(final String statementId, final FilterSpecCompiled filterSpec, FilterService filterService, EPStatementHandle epStatementHandle, boolean isJoin, final boolean isSubSelect, final ExprEvaluatorContext exprEvaluatorContext, boolean isNamedWindowTrigger)
    {
        if (log.isDebugEnabled())
        {
            log.debug(".createStream hashCode=" + filterSpec.hashCode() + " filter=" + filterSpec);
        }

        // Check if a stream for this filter already exists
        Pair<EventStream, EPStatementHandleCallback> pair;
        boolean forceNewStream = isJoin || (!isReuseViews) || isSubSelect || isNamedWindowTrigger;
        if (forceNewStream)
        {
            pair = eventStreamsIdentity.get(filterSpec);
        }
        else
        {
            pair = eventStreamsRefCounted.get(filterSpec);
        }

        // If pair exists, either reference count or illegal state
        if (pair != null)
        {
            if (forceNewStream)
            {
                throw new IllegalStateException("Filter spec object already found in collection");
            }
            else
            {
                log.debug(".createStream filter already found");
                eventStreamsRefCounted.reference(filterSpec);
                // We return the lock of the statement first establishing the stream to use that as the new statement's lock
                return new Pair<EventStream, StatementLock>(pair.getFirst(), pair.getSecond().getEpStatementHandle().getStatementLock());
            }
        }

        // New event stream
        EventType resultEventType = filterSpec.getResultEventType();
        final EventStream eventStream = new ZeroDepthStream(resultEventType);

        FilterHandleCallback filterCallback;
        if (filterSpec.getOptionalPropertyEvaluator() != null)
        {
            filterCallback = new FilterHandleCallback()
            {
                public String getStatementId()
                {
                    return statementId;
                }

                public void matchFound(EventBean event)
                {
                    EventBean[] result = filterSpec.getOptionalPropertyEvaluator().getProperty(event, exprEvaluatorContext);
                    if (result == null)
                    {
                        return;
                    }
                    eventStream.insert(result);
                }

                public boolean isSubSelect()
                {
                    return isSubSelect;
                }
            };
        }
        else
        {
            filterCallback = new FilterHandleCallback()
            {
                public String getStatementId()
                {
                    return statementId;
                }

                public void matchFound(EventBean event)
                {
                    eventStream.insert(event);
                }
                public boolean isSubSelect()
                {
                    return isSubSelect;
                }
            };
        }
        EPStatementHandleCallback handle = new EPStatementHandleCallback(epStatementHandle, filterCallback);

        // Store stream for reuse
        pair = new Pair<EventStream, EPStatementHandleCallback>(eventStream, handle);
        if (forceNewStream)
        {
            eventStreamsIdentity.put(filterSpec, pair);
        }
        else
        {
            eventStreamsRefCounted.put(filterSpec, pair);
        }

        // Activate filter
        FilterValueSet filterValues = filterSpec.getValueSet(null);
        filterService.add(filterValues, handle);

        return new Pair<EventStream, StatementLock>(eventStream, null);
    }

    /**
     * See the method of the same name in {@link com.espertech.esper.view.stream.StreamFactoryService}.
     * @param filterSpec is the filter definition
     */
    public void dropStream(FilterSpecCompiled filterSpec, FilterService filterService, boolean isJoin, boolean isSubSelect, boolean isNamedWindowTrigger)
    {
        Pair<EventStream, EPStatementHandleCallback> pair;
        boolean forceNewStream = isJoin || (!isReuseViews) || isSubSelect || isNamedWindowTrigger;

        if (forceNewStream)
        {
            pair = eventStreamsIdentity.get(filterSpec);
            if (pair == null)
            {
                throw new IllegalStateException("Filter spec object not in collection");
            }
            eventStreamsIdentity.remove(filterSpec);
            filterService.remove(pair.getSecond());
        }
        else
        {
            pair = eventStreamsRefCounted.get(filterSpec);
            boolean isLast = eventStreamsRefCounted.dereference(filterSpec);
            if (isLast)
            {
                filterService.remove(pair.getSecond());
            }
        }
    }
}
