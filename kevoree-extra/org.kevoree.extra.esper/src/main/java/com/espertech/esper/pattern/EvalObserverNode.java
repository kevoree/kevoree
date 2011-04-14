/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import com.espertech.esper.pattern.observer.ObserverFactory;
import com.espertech.esper.epl.spec.PatternObserverSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class represents an observer expression in the evaluation tree representing an pattern expression.
 */
public class EvalObserverNode extends EvalNode
{
    private final PatternObserverSpec patternObserverSpec;
    private transient ObserverFactory observerFactory;
    private transient PatternContext context;    
    private static final long serialVersionUID = 9045310817018028026L;

    /**
     * Constructor.
     * @param patternObserverSpec is the factory to use to get an observer instance
     */
    protected EvalObserverNode(PatternObserverSpec patternObserverSpec)
    {
        this.patternObserverSpec = patternObserverSpec;
    }

    /**
     * Returns the observer object specification to use for instantiating the observer factory and observer.
     * @return observer specification
     */
    public PatternObserverSpec getPatternObserverSpec()
    {
        return patternObserverSpec;
    }

    /**
     * Supplies the observer factory to the node.
     * @param observerFactory is the observer factory
     */
    public void setObserverFactory(ObserverFactory observerFactory)
    {
        this.observerFactory = observerFactory;
    }

    /**
     * Returns the observer factory.
     * @return factory for observer instances
     */
    public ObserverFactory getObserverFactory()
    {
        return observerFactory;
    }

    public EvalStateNode newState(Evaluator parentNode,
                                        MatchedEventMap beginState,
                                        PatternContext context,
                                        EvalStateNodeNumber stateNodeId)
    {
        if (this.context == null) {
            this.context = context;
        }
        return new EvalObserverStateNode(parentNode, this, beginState);
    }

    public PatternContext getContext() {
        return context;
    }

    public final String toString()
    {
        return ("EvalObserverNode observerFactory=" + observerFactory +
                "  children=" + this.getChildNodes().size());
    }

    private static final Log log = LogFactory.getLog(EvalObserverNode.class);
}
