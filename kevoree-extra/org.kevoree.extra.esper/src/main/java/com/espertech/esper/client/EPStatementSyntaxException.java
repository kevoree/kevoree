/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client;

import com.espertech.esper.client.EPStatementException;
import com.espertech.esper.epl.generated.EsperEPL2GrammarParser;
import com.espertech.esper.epl.generated.EsperEPL2Ast;
import org.antlr.runtime.*;

import java.util.Stack;
import java.util.Set;

/**
 * This exception is thrown to indicate a problem in statement creation.
 */
public class EPStatementSyntaxException extends EPStatementException
{
    private static final long serialVersionUID = -1042773433127517692L;

    /**
     * Ctor.
     * @param message - error message
     * @param expression - expression text
     */
    public EPStatementSyntaxException(String message, String expression)
    {
        super(message, expression);
    }
}




