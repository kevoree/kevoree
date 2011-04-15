/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Simple read-write lock based on {@link java.util.concurrent.locks.ReentrantReadWriteLock} that associates a
 * name with the lock and traces read/write locking and unlocking.
 */
public class StatementRWLockImpl implements StatementLock
{
    private static final Log log = LogFactory.getLog(StatementRWLockImpl.class);

    private final ReentrantReadWriteLock.WriteLock writeLock;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final String name;

    /**
     * Ctor.
     * @param name of lock
     * @param isFair true if a fair lock, false if not
     */
    public StatementRWLockImpl(String name, boolean isFair)
    {
        this.name = name;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock(isFair);
        writeLock = lock.writeLock();
        readLock = lock.readLock();
    }

    /**
     * Lock write lock.
     */
    public void acquireWriteLock(StatementLockFactory statementLockFactory)
    {
        writeLock.lock();
    }

    /**
     * Unlock write lock.
     */
    public void releaseWriteLock(StatementLockFactory statementLockFactory)
    {
        writeLock.unlock();
    }

    /**
     * Lock read lock.
     */
    public void acquireReadLock()
    {
        readLock.lock();
    }

    /**
     * Unlock read lock.
     */
    public void releaseReadLock()
    {
        readLock.unlock();
    }

    public String toString()
    {
        return this.getClass().getSimpleName() + " name=" + name;
    }
}
