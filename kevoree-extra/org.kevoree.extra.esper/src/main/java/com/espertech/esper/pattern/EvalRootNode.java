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
 * This class is always the root node in the evaluation tree representing an event expression.
 * It hold the handle to the EPStatement implementation for notifying when matches are found.
 */
public class EvalRootNode extends EvalNode implements PatternStarter
{
    private static final long serialVersionUID = 6894059650449481615L;

    private transient PatternContext context;

    public EvalRootNode() {
    }

    public final PatternStopCallback start(PatternMatchCallback callback,
                                           PatternContext context)
    {
        MatchedEventMap beginState = new MatchedEventMapImpl();
        EvalStateNode rootStateNode = newState(null, beginState, context, null);
        EvalRootState rootState = (EvalRootState) rootStateNode;
        rootState.setCallback(callback);
        rootStateNode.start();
        return rootState;
    }

    public EvalStateNode newState(Evaluator parentNode,
                                        MatchedEventMap beginState,
                                        PatternContext context, EvalStateNodeNumber stateNodeId)
    {
        if (this.context == null) {
            this.context = context;
        }
        return new EvalRootStateNode(this.getChildNodes().get(0), beginState, context);
    }

    public PatternContext getContext() {
        return context;
    }

    public final String toString()
    {
        return ("EvalRootNode children=" + this.getChildNodes().size());
    }

    private static final Log log = LogFactory.getLog(EvalRootNode.class);
}
