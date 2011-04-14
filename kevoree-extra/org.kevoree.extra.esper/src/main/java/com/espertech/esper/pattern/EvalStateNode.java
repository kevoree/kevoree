/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

/**
 * Superclass of all state nodes in an evaluation node tree representing an event expressions.
 * Follows the Composite pattern. Subclasses are expected to keep their own collection containing child nodes
 * as needed.
 */
public abstract class EvalStateNode
{
    private Evaluator parentEvaluator;
    private final EvalStateNodeNumber stateObjectId;

    /**
     * Starts the event expression or an instance of it.
     * Child classes are expected to initialize and start any event listeners
     * or schedule any time-based callbacks as needed.
     */
    public abstract void start();

    /**
     * Stops the event expression or an instance of it. Child classes are expected to free resources
     * and stop any event listeners or remove any time-based callbacks.
     */
    public abstract void quit();

    /**
     * Accept a visitor. Child classes are expected to invoke the visit method on the visitor instance
     * passed in.
     * @param visitor on which the visit method is invoked by each node
     * @param data any additional data the visitor may need is passed in this parameter
     * @return any additional data the visitor may need or null
     */
    public abstract Object accept(EvalStateNodeVisitor visitor, Object data);

    /**
     * Pass the visitor to all child nodes.
     * @param visitor is the instance to be passed to all child nodes
     * @param data any additional data the visitor may need is passed in this parameter
     * @return any additional data the visitor may need or null
     */
    public abstract Object childrenAccept(EvalStateNodeVisitor visitor, Object data);

    /**
     * Returns the factory node for the state node.
     * @return factory node
     */
    public abstract EvalNode getFactoryNode();

    /**
     * Constructor.
     * @param parentNode is the evaluator for this node on which to indicate a change in truth value
     * @param stateObjectId is the state object id assigned to state node
     */
    public EvalStateNode(Evaluator parentNode, EvalStateNodeNumber stateObjectId)
    {
        this.parentEvaluator = parentNode;
        this.stateObjectId = stateObjectId;
    }

    /**
     * Returns the parent evaluator.
     * @return parent evaluator instance
     */
    public final Evaluator getParentEvaluator()
    {
        return parentEvaluator;
    }

    /**
     * Returns the state nodes object id.
     * @return object id
     */
    public Object getStateObjectId()
    {
        return stateObjectId;
    }

    /**
     * Sets the parent evaluator.
     * @param parentEvaluator for this node
     */
    public final void setParentEvaluator(Evaluator parentEvaluator)
    {
        this.parentEvaluator = parentEvaluator;
    }
}
