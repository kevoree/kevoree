/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.client.*;
import com.espertech.esper.collection.SafeIteratorImpl;
import com.espertech.esper.collection.SingleEventIterator;
import com.espertech.esper.dispatch.DispatchService;
import com.espertech.esper.timer.TimeSourceService;
import com.espertech.esper.view.Viewable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Statement implementation for EPL statements.
 */
public class EPStatementImpl implements EPStatementSPI
{
    private static Log log = LogFactory.getLog(EPStatementImpl.class);

    private final EPStatementListenerSet statementListenerSet;
    private final String statementId;
    private final String statementName;
    private final String expressionText;
    private final String expressionNoAnnotations;
    private final boolean nameProvided;
    private boolean isPattern;
    private UpdateDispatchViewBase dispatchChildView;
    private StatementLifecycleSvc statementLifecycleSvc;

    private long timeLastStateChange;
    private Viewable parentView;
    private EPStatementState currentState;
    private EventType eventType;
    private StatementMetadata statementMetadata;
    private Object userObject;
    private Annotation[] annotations;
    private StatementContext statementContext;
    private String serviceIsolated;

    /**
     * Ctor.
     * @param statementId is a unique ID assigned by the engine for the statement
     * @param statementName is the statement name assigned during creation, or the statement id if none was assigned
     * @param expressionText is the EPL and/or pattern expression
     * @param isPattern is true to indicate this is a pure pattern expression
     * @param dispatchService for dispatching events to listeners to the statement
     * @param statementLifecycleSvc handles lifecycle transitions for the statement
     * @param isBlockingDispatch is true if the dispatch to listeners should block to preserve event generation order
     * @param isSpinBlockingDispatch true to use spin locks blocking to deliver results, as locks are usually uncontended
     * @param msecBlockingTimeout is the max number of milliseconds of block time
     * @param timeLastStateChange the timestamp the statement was created and started
     * @param timeSourceService time source provider
     * @param statementMetadata statement metadata
     * @param userObject the application define user object associated to each statement, if supplied
     * @param annotations annotations associated to statement
     * @param statementContext the statement service context
     * @param expressionNoAnnotations expression text witout annotations
     * @param isFailed indicator to start in failed state
     * @param nameProvided true to indicate a statement name has been provided and is not a system-generated name
     */
    public EPStatementImpl(String statementId,
                              String statementName,
                              String expressionText,
                              String expressionNoAnnotations,
                              boolean isPattern,
                              DispatchService dispatchService,
                              StatementLifecycleSvc statementLifecycleSvc,
                              long timeLastStateChange,
                              boolean isBlockingDispatch,
                              boolean isSpinBlockingDispatch,
                              long msecBlockingTimeout,
                              TimeSourceService timeSourceService,
                              StatementMetadata statementMetadata,
                              Object userObject,
                              Annotation[] annotations,
                              StatementContext statementContext,
                              boolean isFailed,
                              boolean nameProvided)
    {
        this.isPattern = isPattern;
        this.statementId = statementId;
        this.statementName = statementName;
        this.expressionText = expressionText;
        this.expressionNoAnnotations = expressionNoAnnotations;
        this.statementLifecycleSvc = statementLifecycleSvc;
        this.statementContext = statementContext;
        this.nameProvided = nameProvided; 
        statementListenerSet = new EPStatementListenerSet();
        if (isBlockingDispatch)
        {
            if (isSpinBlockingDispatch)
            {
                this.dispatchChildView = new UpdateDispatchViewBlockingSpin(statementContext.getStatementResultService(), dispatchService, msecBlockingTimeout, timeSourceService);
            }
            else
            {
                this.dispatchChildView = new UpdateDispatchViewBlockingWait(statementContext.getStatementResultService(), dispatchService, msecBlockingTimeout);
            }
        }
        else
        {
            this.dispatchChildView = new UpdateDispatchViewNonBlocking(statementContext.getStatementResultService(), dispatchService);
        }
        if (!isFailed) {
            this.currentState = EPStatementState.STOPPED;
        }
        else {
            this.currentState = EPStatementState.FAILED;
        }
        this.timeLastStateChange = timeLastStateChange;
        this.statementMetadata = statementMetadata;
        this.userObject = userObject;
        this.annotations = annotations;
        statementContext.getStatementResultService().setUpdateListeners(statementListenerSet);
    }

    public String getStatementId()
    {
        return statementId;
    }

    public void start()
    {
        if (statementLifecycleSvc == null)
        {
            throw new IllegalStateException("Cannot start statement, statement is in destroyed state");
        }
        statementLifecycleSvc.start(statementId);
    }

    public void stop()
    {
        if (statementLifecycleSvc == null)
        {
            throw new IllegalStateException("Cannot stop statement, statement is in destroyed state");
        }
        statementLifecycleSvc.stop(statementId);

        // On stop, we give the dispatch view a chance to dispatch final results, if any
        statementContext.getStatementResultService().dispatchOnStop();

        dispatchChildView.clear();
    }

    public void destroy()
    {
        if (currentState == EPStatementState.DESTROYED)
        {
            throw new IllegalStateException("Statement already destroyed");
        }
        statementLifecycleSvc.destroy(statementId);
        parentView = null;
        eventType = null;
        dispatchChildView = null;
        statementLifecycleSvc = null;
    }

    public EPStatementState getState()
    {
        return currentState;
    }

    public void setCurrentState(EPStatementState currentState, long timeLastStateChange)
    {
        this.currentState = currentState;
        this.timeLastStateChange = timeLastStateChange;
    }

    public void setParentView(Viewable viewable)
    {
        if (viewable == null)
        {
            if (parentView != null)
            {
                parentView.removeView(dispatchChildView);
            }
            parentView = null;
        }
        else
        {
            parentView = viewable;
            parentView.addView(dispatchChildView);
            eventType = parentView.getEventType();
        }
    }

    public String getText()
    {
        return expressionText;
    }

    public String getName()
    {
        return statementName;
    }

    public Iterator<EventBean> iterator()
    {
        // Return null if not started
        statementContext.getVariableService().setLocalVersion();
        if (parentView == null)
        {
            return null;
        }
        if (isPattern)
        {
            return new SingleEventIterator(statementContext.getStatementResultService().getLastIterableEvent());
        }
        else
        {
            return parentView.iterator();
        }
    }

    public SafeIterator<EventBean> safeIterator()
    {
        // Return null if not started
        if (parentView == null)
        {
            return null;
        }

        // Set variable version and acquire the lock first
        statementContext.getEpStatementHandle().getStatementLock().acquireReadLock();
        try
        {
            statementContext.getVariableService().setLocalVersion();

            // Provide iterator - that iterator MUST be closed else the lock is not released
            if (isPattern)
            {
                return new SafeIteratorImpl<EventBean>(statementContext.getEpStatementHandle().getStatementLock(), dispatchChildView.iterator());
            }
            else
            {
                return new SafeIteratorImpl<EventBean>(statementContext.getEpStatementHandle().getStatementLock(), parentView.iterator());
            }
        }
        catch (RuntimeException ex)
        {
            statementContext.getEpStatementHandle().getStatementLock().releaseReadLock();
            throw ex;
        }
    }

    public EventType getEventType()
    {
        return eventType;
    }

    /**
     * Returns the set of listeners to the statement.
     * @return statement listeners
     */
    public EPStatementListenerSet getListenerSet()
    {
        return statementListenerSet;
    }

    public void setListeners(EPStatementListenerSet listenerSet)
    {
        statementListenerSet.setListeners(listenerSet);
        statementContext.getStatementResultService().setUpdateListeners(listenerSet);
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
        if (isDestroyed())
        {
            throw new IllegalStateException("Statement is in destroyed state");
        }

        statementListenerSet.addListener(listener);
        statementContext.getStatementResultService().setUpdateListeners(statementListenerSet);
        statementLifecycleSvc.dispatchStatementLifecycleEvent(
                new StatementLifecycleEvent(this, StatementLifecycleEvent.LifecycleEventType.LISTENER_ADD, listener));
    }

    public void addListenerWithReplay(UpdateListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("Null listener reference supplied");
        }

        if (isDestroyed())
        {
            throw new IllegalStateException("Statement is in destroyed state");
        }

        statementContext.getEpStatementHandle().getStatementLock().acquireReadLock();
        try
        {
            // Add listener - listener not receiving events from this statement, as the statement is locked
            statementListenerSet.addListener(listener);
            statementContext.getStatementResultService().setUpdateListeners(statementListenerSet);
            statementLifecycleSvc.dispatchStatementLifecycleEvent(
                    new StatementLifecycleEvent(this, StatementLifecycleEvent.LifecycleEventType.LISTENER_ADD, listener));

            Iterator<EventBean> it = iterator();
            if (it == null)
            {
                try
                {
                    listener.update(null, null);
                }
                catch (Throwable t)
                {
                    String message = "Unexpected exception invoking listener update method for replay on listener class '" + listener.getClass().getSimpleName() +
                            "' : " + t.getClass().getSimpleName() + " : " + t.getMessage();
                    log.error(message, t);
                }
                return;
            }

            ArrayList<EventBean> events = new ArrayList<EventBean>();
            for (; it.hasNext();)
            {
                events.add(it.next());
            }

            if (events.isEmpty())
            {
                try
                {
                    listener.update(null, null);
                }
                catch (Throwable t)
                {
                    String message = "Unexpected exception invoking listener update method for replay on listener class '" + listener.getClass().getSimpleName() +
                            "' : " + t.getClass().getSimpleName() + " : " + t.getMessage();
                    log.error(message, t);
                }
            }
            else
            {
                EventBean[] iteratorResult = events.toArray(new EventBean[events.size()]);
                try
                {
                    listener.update(iteratorResult, null);
                }
                catch (Throwable t)
                {
                    String message = "Unexpected exception invoking listener update method for replay on listener class '" + listener.getClass().getSimpleName() +
                            "' : " + t.getClass().getSimpleName() + " : " + t.getMessage();
                    log.error(message, t);
                }
            }
        }
        finally
        {
            statementContext.getEpStatementHandle().getStatementLock().releaseReadLock();
        }
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

        statementListenerSet.removeListener(listener);
        statementContext.getStatementResultService().setUpdateListeners(statementListenerSet);
        if (statementLifecycleSvc != null)
        {
            statementLifecycleSvc.dispatchStatementLifecycleEvent(
                new StatementLifecycleEvent(this, StatementLifecycleEvent.LifecycleEventType.LISTENER_REMOVE, listener));
        }
    }

    /**
     * Remove all listeners to a statement.
     */
    public void removeAllListeners()
    {
        statementListenerSet.removeAllListeners();
        statementContext.getStatementResultService().setUpdateListeners(statementListenerSet);
        if (statementLifecycleSvc != null)
        {
            statementLifecycleSvc.dispatchStatementLifecycleEvent(
                new StatementLifecycleEvent(this, StatementLifecycleEvent.LifecycleEventType.LISTENER_REMOVE_ALL));
        }
    }

    public void addListener(StatementAwareUpdateListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("Null listener reference supplied");
        }
        if (isDestroyed())
        {
            throw new IllegalStateException("Statement is in destroyed state");
        }

        statementListenerSet.addListener(listener);
        statementContext.getStatementResultService().setUpdateListeners(statementListenerSet);
        statementLifecycleSvc.dispatchStatementLifecycleEvent(
                new StatementLifecycleEvent(this, StatementLifecycleEvent.LifecycleEventType.LISTENER_ADD, listener));
    }

    public void removeListener(StatementAwareUpdateListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("Null listener reference supplied");
        }

        statementListenerSet.removeListener(listener);
        statementContext.getStatementResultService().setUpdateListeners(statementListenerSet);
        if (statementLifecycleSvc != null)
        {
            statementLifecycleSvc.dispatchStatementLifecycleEvent(
                new StatementLifecycleEvent(this, StatementLifecycleEvent.LifecycleEventType.LISTENER_REMOVE, listener));
        }
    }

    public Iterator<StatementAwareUpdateListener> getStatementAwareListeners()
    {
        return statementListenerSet.getStmtAwareListeners().iterator();
    }

    public Iterator<UpdateListener> getUpdateListeners()
    {
        return statementListenerSet.getListeners().iterator();
    }

    public long getTimeLastStateChange()
    {
        return timeLastStateChange;
    }

    public boolean isStarted()
    {
        return currentState == EPStatementState.STARTED;
    }

    public boolean isStopped()
    {
        return currentState == EPStatementState.STOPPED;
    }

    public boolean isDestroyed()
    {
        return currentState == EPStatementState.DESTROYED;
    }

    public void setSubscriber(Object subscriber)
    {
        statementListenerSet.setSubscriber(subscriber);
        statementContext.getStatementResultService().setUpdateListeners(statementListenerSet);
    }

    public Object getSubscriber()
    {
        return statementListenerSet.getSubscriber();
    }

    public boolean isPattern() {
        return isPattern;
    }

    public StatementMetadata getStatementMetadata()
    {
        return statementMetadata;
    }

    public Object getUserObject()
    {
        return userObject;
    }

    public Annotation[] getAnnotations()
    {
        return annotations;
    }

    public StatementContext getStatementContext()
    {
        return statementContext;
    }

    public String getExpressionNoAnnotations()
    {
        return expressionNoAnnotations;
    }

    public String getServiceIsolated()
    {
        return serviceIsolated;
    }

    public void setServiceIsolated(String serviceIsolated)
    {
        this.serviceIsolated = serviceIsolated;
    }

    public boolean isNameProvided()
    {
        return nameProvided;
    }
}
