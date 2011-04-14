/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.StatementAwareUpdateListener;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Provides update listeners for use by statement instances, and the management methods around these.
 * <p>
 * The collection of update listeners is based on copy-on-write:
 * When the engine dispatches events to a set of listeners, then while iterating through the set there
 * may be listeners added or removed (the listener may remove itself).
 * Additionally, events may be dispatched by multiple threads to the same listener.
 */
public class EPStatementListenerSet
{
    Object subscriber;
    CopyOnWriteArraySet<UpdateListener> listeners;
    CopyOnWriteArraySet<StatementAwareUpdateListener> stmtAwareListeners;


    /**
     * Ctor.
     */
    public EPStatementListenerSet()
    {
        listeners = new CopyOnWriteArraySet<UpdateListener>();
        stmtAwareListeners = new CopyOnWriteArraySet<StatementAwareUpdateListener>();
    }

    /**
     * Ctor.
     * @param listeners is a set of update listener
     * @param stmtAwareListeners is a set of statement-aware update listener
     */
    public EPStatementListenerSet(CopyOnWriteArraySet<UpdateListener> listeners, CopyOnWriteArraySet<StatementAwareUpdateListener> stmtAwareListeners)
    {
        this.listeners = listeners;
        this.stmtAwareListeners = stmtAwareListeners;
    }

    /**
     * Returns the set of listeners to the statement.
     * @return statement listeners
     */
    public CopyOnWriteArraySet<UpdateListener> getListeners()
    {
        return listeners;
    }

    /**
     * Returns the set of statement-aware listeners.
     * @return statement-aware listeners
     */
    public CopyOnWriteArraySet<StatementAwareUpdateListener> getStmtAwareListeners()
    {
        return stmtAwareListeners;
    }

    /**
     * Set the update listener set to use.
     * @param listenerSet a collection of update listeners
     */
    public void setListeners(EPStatementListenerSet listenerSet)
    {
        this.listeners = listenerSet.getListeners();
        this.stmtAwareListeners = listenerSet.getStmtAwareListeners();
    }

    /**
     * Add a listener to the statement.
     * @param listener to add
     */
    public void addListener(UpdateListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("Null listener reference supplied");
        }

        listeners.add(listener);
    }

    /**
     * Remove a listeners to a statement.
     * @param listener to remove
     */
    public void removeListener(UpdateListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("Null listener reference supplied");
        }

        listeners.remove(listener);
    }

    /**
     * Remove all listeners to a statement.
     */
    public void removeAllListeners()
    {
        listeners.clear();
        stmtAwareListeners.clear();
    }

    /**
     * Add a listener to the statement.
     * @param listener to add
     */
    public void addListener(StatementAwareUpdateListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("Null listener reference supplied");
        }

        stmtAwareListeners.add(listener);
    }

    /**
     * Remove a listeners to a statement.
     * @param listener to remove
     */
    public void removeListener(StatementAwareUpdateListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("Null listener reference supplied");
        }

        stmtAwareListeners.remove(listener);
    }

    /**
     * Sets a subscriber instance.
     * @param subscriber is the subscriber to set
     */
    public void setSubscriber(Object subscriber)
    {
        this.subscriber = subscriber;
    }

    /**
     * Returns the subscriber instance.
     * @return subscriber
     */
    public Object getSubscriber()
    {
        return subscriber;
    }
}
