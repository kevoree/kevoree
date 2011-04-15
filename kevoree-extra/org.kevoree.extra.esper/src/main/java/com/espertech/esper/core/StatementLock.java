/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

// 48sec for 1000000000 for plain ReentrantLock
// 60sec for 1000000000 for ReentrantReadWriteLock
// 55sec for 1000000000 for ReentrantReadWriteLock.WriteLock
// 48sec for 1000000000 for LockInterface-based ReentrantLock
// 59sec for 1000000000 for LockInterface-based ReentrantReadWriteLock
public interface StatementLock
{
    /**
     * Acquire text.
     */
    public final static String ACQUIRE_TEXT  = "Acquire ";

    /**
     * Acquired text.
     */
    public final static String ACQUIRED_TEXT = "Got     ";

    /**
     * Release text.
     */
    public final static String RELEASE_TEXT  = "Release ";

    /**
     * Released text.
     */
    public final static String RELEASED_TEXT = "Freed   ";

    public void acquireWriteLock(StatementLockFactory statementLockFactory);
    public void releaseWriteLock(StatementLockFactory statementLockFactory);
    public void acquireReadLock();
    public void releaseReadLock();
}
