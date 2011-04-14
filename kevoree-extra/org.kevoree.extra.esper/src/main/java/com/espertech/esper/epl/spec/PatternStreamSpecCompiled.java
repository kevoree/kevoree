/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.collection.Pair;
import com.espertech.esper.client.EventType;
import com.espertech.esper.pattern.EvalNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Specification for building an event stream out of a pattern statement and views staggered onto the
 * pattern statement.
 * <p>
 * The pattern statement is represented by the top EvalNode evaluation node.
 * A pattern statement contains tagged events (i.e. a=A -> b=B).
 * Thus the resulting event type is has properties "a" and "b" of the type of A and B.
 */
public class PatternStreamSpecCompiled extends StreamSpecBase implements StreamSpecCompiled
{
    private final EvalNode evalNode;
    private final Map<String, Pair<EventType, String>> taggedEventTypes;       // Stores types for filters with tags, single event
    private final Map<String, Pair<EventType, String>> arrayEventTypes;       // Stores types for filters with tags, array event
    private static final long serialVersionUID = 1268004301792124753L;

    /**
     * Ctor.
     * @param evalNode - pattern evaluation node representing pattern statement
     * @param viewSpecs - specifies what view to use to derive data
     * @param taggedEventTypes - event tags and their types as specified in the pattern, copied to allow original collection to change
     * @param arrayEventTypes - event tags and their types as specified in the pattern for any repeat-expressions that generate an array of events
     * @param optionalStreamName - stream name, or null if none supplied
     * @param streamSpecOptions - additional stream options such as unidirectional stream in a join, applicable for joins
     */
    public PatternStreamSpecCompiled(EvalNode evalNode, Map<String, Pair<EventType, String>> taggedEventTypes, Map<String, Pair<EventType, String>> arrayEventTypes, List<ViewSpec> viewSpecs, String optionalStreamName, StreamSpecOptions streamSpecOptions)
    {
        super(optionalStreamName, viewSpecs, streamSpecOptions);
        this.evalNode = evalNode;

        Map<String, Pair<EventType, String>> copy = new HashMap<String, Pair<EventType, String>>();
        copy.putAll(taggedEventTypes);
        this.taggedEventTypes = copy;

        copy = new HashMap<String, Pair<EventType, String>>();
        copy.putAll(arrayEventTypes);
        this.arrayEventTypes = copy;
    }

    /**
     * Returns the pattern expression evaluation node for the top pattern operator.
     * @return parent pattern expression node
     */
    public EvalNode getEvalNode()
    {
        return evalNode;
    }

    /**
     * Returns event types tagged in the pattern expression.
     * @return map of tag and event type tagged in pattern expression
     */
    public Map<String, Pair<EventType, String>> getTaggedEventTypes()
    {
        return taggedEventTypes;
    }

    /**
     * Returns event types tagged in the pattern expression under a repeat-operator.
     * @return map of tag and event type tagged in pattern expression, repeated an thus producing array events
     */
    public Map<String, Pair<EventType, String>> getArrayEventTypes()
    {
        return arrayEventTypes;
    }
}
