/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.metric.StatementMetricHandle;
import com.espertech.esper.util.ManagedReadWriteLock;
import com.espertech.esper.util.MetaDefItem;

import java.io.Serializable;

/**
 * Class exists once per statement and hold statement resource lock(s).
 * <p>
 * Use by {@link EPRuntimeImpl} for determining callback-statement affinity and locking of statement
 * resources.
 */
public class EPStatementHandle implements MetaDefItem, Serializable
{
    private static final long serialVersionUID = 0L;

    private final String statementName;
    private final String statementId;
    private final String statementText;
    private transient StatementLock statementLock = null;
    private final int hashCode;
    private transient EPStatementDispatch optionalDispatchable;
    // handles self-join (ie. statement where from-clause lists the same event type or a super-type more then once)
    // such that the internal dispatching must occur after both matches are processed
    private boolean canSelfJoin;
    private boolean hasVariables;
    private final int priority;
    private final boolean preemptive;
    private transient InsertIntoLatchFactory insertIntoFrontLatchFactory;
    private transient InsertIntoLatchFactory insertIntoBackLatchFactory;
    private transient StatementMetricHandle metricsHandle = null;
    private final StatementFilterVersion statementFilterVersion;

    /**
     * Ctor.
     * @param statementId is the statement id uniquely indentifying the handle
     * @param statementLock is the statement resource lock
     * @param expressionText is the expression
     * @param hasVariables indicator whether the statement uses variables
     * @param metricsHandle handle for metrics reporting
     * @param priority priority, zero is default
     * @param preemptive true for drop after done
     * @param statementFilterVersion filter version
     */
    public EPStatementHandle(String statementId, String statementName, String statementText, StatementLock statementLock, String expressionText, boolean hasVariables, StatementMetricHandle metricsHandle, int priority, boolean preemptive, StatementFilterVersion statementFilterVersion)
    {
        this.statementId = statementId;
        this.statementName = statementName;
        this.statementText = statementText;
        this.statementLock = statementLock;
        this.hasVariables = hasVariables;
        this.metricsHandle = metricsHandle;
        this.priority = priority;
        this.preemptive = preemptive;
        this.statementFilterVersion = statementFilterVersion;
        hashCode = expressionText.hashCode() ^ statementLock.hashCode();
    }

    /**
     * Tests filter version.
     * @param filterVersion to test
     * @return indicator whether version is up-to-date
     */
    public boolean isCurrentFilter(long filterVersion) {
        return statementFilterVersion.isCurrentFilter(filterVersion);
    }

    /**
     * Set the statement's self-join flag to indicate the the statement may join to itself,
     * that is a single event may dispatch into multiple streams or patterns for the same statement,
     * requiring internal dispatch logic to not shortcut evaluation of all filters for the statement
     * within one lock, requiring the callback handle to be sorted.
     * @param canSelfJoin is true if the statement potentially self-joins, false if not
     */
    public void setCanSelfJoin(boolean canSelfJoin)
    {
        this.canSelfJoin = canSelfJoin;
    }

    /**
     * Returns the statement id.
     * @return statement id
     */
    public String getStatementId() {
        return statementId;
    }

    /**
     * Sets the factory for latches in insert-into guaranteed order of delivery.
     * @param insertIntoFrontLatchFactory latch factory for the statement if it performs insert-into (route) of events
     */
    public void setInsertIntoFrontLatchFactory(InsertIntoLatchFactory insertIntoFrontLatchFactory)
    {
        this.insertIntoFrontLatchFactory = insertIntoFrontLatchFactory;
    }

    public void setInsertIntoBackLatchFactory(InsertIntoLatchFactory insertIntoBackLatchFactory) {
        this.insertIntoBackLatchFactory = insertIntoBackLatchFactory;
    }

    /**
     * Returns the factory for latches in insert-into guaranteed order of delivery.
     * @return latch factory for the statement if it performs insert-into (route) of events
     */
    public InsertIntoLatchFactory getInsertIntoFrontLatchFactory()
    {
        return insertIntoFrontLatchFactory;
    }

    public InsertIntoLatchFactory getInsertIntoBackLatchFactory() {
        return insertIntoBackLatchFactory;
    }

    /**
     * Returns statement resource lock.
     * @return lock
     */
    public StatementLock getStatementLock()
    {
        return statementLock;
    }

    /**
     * Returns true if the statement uses variables, false if not.
     * @return indicator if variables are used by statement
     */
    public boolean isHasVariables()
    {
        return hasVariables;
    }

    /**
     * Sets the lock to use for the statement.
     * @param statementLock statement lock
     */
    public void setStatementLock(StatementLock statementLock)
    {
        this.statementLock = statementLock;
    }

    /**
     * Provides a callback for use when statement processing for filters and schedules is done,
     * for use by join statements that require an explicit indicator that all
     * joined streams results have been processed.
     * @param optionalDispatchable is the instance for calling onto after statement callback processing
     */
    public void setOptionalDispatchable(EPStatementDispatch optionalDispatchable)
    {
        this.optionalDispatchable = optionalDispatchable;
    }

    /**
     * Invoked by {@link com.espertech.esper.client.EPRuntime} to indicate that a statements's
     * filer and schedule processing is done, and now it's time to process join results.
     * @param exprEvaluatorContext context for expression evaluation
     */
    public void internalDispatch(ExprEvaluatorContext exprEvaluatorContext)
    {
        if (optionalDispatchable != null)
        {
            optionalDispatchable.execute(exprEvaluatorContext);
        }
    }

    /**
     * Returns the statement priority.
     * @return priority, default 0
     */
    public int getPriority()
    {
        return priority;
    }

    /**
     * True for preemptive (drop) statements.
     * @return preemptive indicator
     */
    public boolean isPreemptive()
    {
        return preemptive;
    }

    public boolean equals(Object otherObj)
    {
        if (!(otherObj instanceof EPStatementHandle))
        {
            return false;
        }

        EPStatementHandle other = (EPStatementHandle) otherObj;
        if (other.statementId.equals(this.statementId))
        {
            return true;
        }
        return false;
    }

    public int hashCode()
    {
        return hashCode;
    }

    /**
     * Returns true if the statement potentially self-joins amojng the events it processes.
     * @return true for self-joins possible, false for not possible (most statements)
     */
    public boolean isCanSelfJoin()
    {
        return canSelfJoin;
    }

    /**
     * Returns handle for metrics reporting.
     * @return handle for metrics reporting
     */
    public StatementMetricHandle getMetricsHandle()
    {
        return metricsHandle;
    }

    /**
     * Returns dispatch.
     * @return dispatch
     */
    public EPStatementDispatch getOptionalDispatchable()
    {
        return optionalDispatchable;
    }

    public String getStatementName() {
        return statementName;
    }

    public String getEPL() {
        return statementText;
    }
}
