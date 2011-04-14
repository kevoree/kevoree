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
 * Interface for visiting each element in the evaluation node tree for an event expression (see Visitor pattern).
 */
public interface EvalStateNodeVisitor
{
    /**
     * Invoked by each child node as part of accepting this visitor.
     * @param node is the node in the composite tree accepting the visitor
     * @param data is any additional useful to implementations
     * @return any additional data useful to implementations or null
     */
    public Object visit(EvalStateNode node, Object data);
}
