/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.schedule;

/**
 * This exception is thrown to indicate trying to add a scheduling callback
 * that already existed.
 */
public final class ScheduleHandleExistsException extends ScheduleServiceException
{
    private static final long serialVersionUID = 7987557146132195955L;

    /**
     * Constructor.
     * @param message is the error message
     */
    public ScheduleHandleExistsException(final String message)
    {
        super(message);
    }

    /**
     * Constructor for an inner exception and message.
     * @param message is the error message
     * @param cause is the inner exception
     */
    public ScheduleHandleExistsException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * @param cause is the inner exception
     */
    public ScheduleHandleExistsException(final Throwable cause)
    {
        super(cause);
    }
}
