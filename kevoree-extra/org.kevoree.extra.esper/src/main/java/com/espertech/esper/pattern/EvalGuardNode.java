/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import com.espertech.esper.pattern.guard.GuardFactory;
import com.espertech.esper.epl.spec.PatternGuardSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a guard in the evaluation tree representing an event expressions.
 */
public class EvalGuardNode extends EvalNode
{
    private PatternGuardSpec patternGuardSpec;
    private transient GuardFactory guardFactory;
    private transient PatternContext context;
    private static final long serialVersionUID = -1300326291593373936L;

    /**
     * Constructor.
     * @param patternGuardSpec - factory for guard construction
     */
    protected EvalGuardNode(PatternGuardSpec patternGuardSpec)
    {
        this.patternGuardSpec = patternGuardSpec;
    }

    /**
     * Returns the guard object specification to use for instantiating the guard factory and guard.
     * @return guard specification
     */
    public PatternGuardSpec getPatternGuardSpec()
    {
        return patternGuardSpec;
    }

    /**
     * Supplies the guard factory to the node.
     * @param guardFactory is the guard factory
     */
    public void setGuardFactory(GuardFactory guardFactory)
    {
        this.guardFactory = guardFactory;
    }

    public EvalStateNode newState(Evaluator parentNode,
                                        MatchedEventMap beginState,
                                        PatternContext context, EvalStateNodeNumber stateNodeId)
    {
        if (this.context == null) {
            this.context = context;
        }
        return new EvalGuardStateNode(parentNode, this, beginState, stateNodeId);
    }

    public PatternContext getContext() {
        return context;
    }

    /**
     * Returns the guard factory.
     * @return guard factory
     */
    public GuardFactory getGuardFactory()
    {
        return guardFactory;
    }

    public final String toString()
    {
        return ("EvalGuardNode guardFactory=" + guardFactory +
                "  children=" + this.getChildNodes().size());
    }

    private static final Log log = LogFactory.getLog(EvalGuardNode.class);
}
