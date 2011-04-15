/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.parse;

import org.antlr.runtime.RecognitionException;

/**
 * For selection of the AST tree walk rule to use.
 */
public interface WalkRuleSelector
{
    /**
     * Implementations can invoke a walk rule of their choice on the walker and AST passed in.
     * @param walker - to invoke walk rule on
     * @throws org.antlr.runtime.RecognitionException - throw on walk errors
     */
    public void invokeWalkRule(EPLTreeWalker walker) throws RecognitionException;
}



