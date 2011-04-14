/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import com.espertech.esper.epl.spec.FilterSpecRaw;
import com.espertech.esper.filter.FilterSpecCompiled;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a filter of events in the evaluation tree representing any event expressions.
 */
public class EvalFilterNode extends EvalNode
{
    private static final long serialVersionUID = 0L;
    private final FilterSpecRaw rawFilterSpec;
    private final String eventAsName;
    private transient FilterSpecCompiled filterSpec;
    private transient PatternContext context;

    /**
     * Constructor.
     * @param filterSpecification specifies the filter properties
     * @param eventAsName is the name to use for adding matching events to the MatchedEventMap
     * table used when indicating truth value of true.
     */
    protected EvalFilterNode(FilterSpecRaw filterSpecification,
                                String eventAsName)
    {
        this.rawFilterSpec = filterSpecification;
        this.eventAsName = eventAsName;
    }

    public EvalStateNode newState(Evaluator parentNode,
                                        MatchedEventMap beginState,
                                        PatternContext context, EvalStateNodeNumber stateNodeId)
    {
        if (this.context == null) {
            this.context = context;
        }
        return new EvalFilterStateNode(parentNode, this, beginState);
    }

    public PatternContext getContext() {
        return context;
    }

    /**
     * Returns the raw (unoptimized/validated) filter definition.
     * @return filter def
     */
    public FilterSpecRaw getRawFilterSpec()
    {
        return rawFilterSpec;
    }

    /**
     * Returns filter specification.
     * @return filter definition
     */
    public final FilterSpecCompiled getFilterSpec()
    {
        return filterSpec;
    }

    /**
     * Sets a validated and optimized filter specification
     * @param filterSpec is the optimized filter
     */
    public void setFilterSpec(FilterSpecCompiled filterSpec)
    {
        this.filterSpec = filterSpec;
    }

    /**
     * Returns the tag for any matching events to this filter, or null since tags are optional.
     * @return tag string for event
     */
    public final String getEventAsName()
    {
        return eventAsName;
    }

    @SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
    public final String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("EvalFilterNode rawFilterSpec=" + this.rawFilterSpec);
        buffer.append(" filterSpec=" + this.filterSpec);
        buffer.append(" eventAsName=" + this.eventAsName);
        return buffer.toString();
    }

    private static final Log log = LogFactory.getLog(EvalFilterNode.class);
}
