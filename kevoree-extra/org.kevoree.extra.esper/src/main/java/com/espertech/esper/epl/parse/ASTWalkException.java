/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.parse;

/**
 * This exception is thrown to indicate a problem in statement creation.
 */
public class ASTWalkException extends RuntimeException
{
    private static final long serialVersionUID = -339092618059394426L;

    /**
     * Ctor.
     * @param message is the error message
     * @param t is the inner throwable
     */
    public ASTWalkException(String message, Throwable t)
    {
        super(message, t);
    }

    /**
     * Ctor.
     * @param message is the error message
     */
    public ASTWalkException(String message)
    {
        super(message);
    }

}

