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
 * This exception is thrown to indicate a problem in a filter specification.
 */
public class ASTFilterSpecValidationException extends ASTWalkException
{
    private static final long serialVersionUID = -2940818233788689612L;

    /**
     * Ctor.
     * @param message - error message
     * @param t - inner throwable
     */
    public ASTFilterSpecValidationException(final String message, Throwable t)
    {
        super(message, t);
    }

    /**
     * Ctor.
     * @param message - error message
     */
    public ASTFilterSpecValidationException(final String message)
    {
        super(message);
    }
}
