/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.filter;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.client.EventType;
import com.espertech.esper.util.ExecutionPathDebugLog;

/**
 * Builder manipulates a tree structure consisting of {@link FilterHandleSetNode} and {@link FilterParamIndexBase} instances.
 * Filters can be added to a top node (an instance of FilterHandleSetNode) via the add method. This method returns
 * an instance of {@link IndexTreePath} which represents the tree path (list of indizes) that the filter callback was
 * added to. To remove filters the same IndexTreePath instance must be passed in.
 * <p>The implementation is designed to be multithread-safe in conjunction with the node classes manipulated by this class.
 */
public final class IndexTreeBuilder
{
    private EventType eventType;
    private SortedSet<FilterValueSetParam> remainingParameters;
    private FilterHandle filterCallback;
    private long currentThreadId;

    /**
     * Constructor.
     */
    public IndexTreeBuilder()
    {
    }

    /**
     * Add a filter callback according to the filter specification to the top node returning
     * information to be used to remove the filter callback.
     * @param filterValueSet is the filter definition
     * @param filterCallback is the callback to be added
     * @param topNode node to be added to any subnode beneath it
     * @return an encapsulation of information need to allow for safe removal of the filter tree.
     */
    public final IndexTreePath add( FilterValueSet filterValueSet,
                                    FilterHandle filterCallback,
                                    FilterHandleSetNode topNode)
    {
        this.eventType = filterValueSet.getEventType();
        this.remainingParameters = copySortParameters(filterValueSet.getParameters());
        this.filterCallback = filterCallback;
        this.currentThreadId = Thread.currentThread().getId();

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".add (" + currentThreadId + ") Adding filter callback, " +
                      "  topNode=" + topNode +
                      "  filterCallback=" + this.filterCallback);
        }

        IndexTreePath treePathInfo = new IndexTreePath();

        addToNode(topNode, treePathInfo);

        this.remainingParameters = null;
        this.filterCallback = null;

        return treePathInfo;
    }

    /**
     * Remove an filterCallback from the given top node. The IndexTreePath instance passed in must be the
     * same as obtained when the same filterCallback was added.
     * @param filterCallback filter callback  to be removed
     * @param treePathInfo encapsulates information need to allow for safe removal of the filterCallback
     * @param topNode The top tree node beneath which the filterCallback was added
     */
    public final void remove(
                       FilterHandle filterCallback,
                       IndexTreePath treePathInfo,
                       FilterHandleSetNode topNode)
    {
        this.remainingParameters = null;
        this.filterCallback = filterCallback;
        this.currentThreadId = Thread.currentThread().getId();

        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".remove (" + currentThreadId + ") Removing filterCallback " +
                      " from treepath=" + treePathInfo.toString() +
                      "  topNode=" + topNode +
                      "  filterCallback=" + filterCallback);
        }

        removeFromNode(topNode, treePathInfo);

        this.filterCallback = null;
    }

    /**
     * Add to the current node building up the tree path information.
     * @param currentNode is the node to add to
     * @param treePathInfo is filled with information about which indizes were chosen to add the filter to
     */
    private void addToNode(FilterHandleSetNode currentNode, IndexTreePath treePathInfo)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".addToNode (" + currentThreadId + ") Adding filterCallback, node=" + currentNode +
                      "  remainingParameters=" + printRemainingParameters());
        }

        // If no parameters are specified, add to current node, and done
        if (remainingParameters.isEmpty())
        {
            currentNode.getNodeRWLock().writeLock().lock();
            try
            {
                currentNode.add(filterCallback);
            }            
            finally
            {
                currentNode.getNodeRWLock().writeLock().unlock();
            }
            return;
        }

        // Need to find an existing index that matches one of the filter parameters
        currentNode.getNodeRWLock().readLock().lock();
        Pair<FilterValueSetParam, FilterParamIndexBase> pair;
        try
        {
            pair = IndexHelper.findIndex(remainingParameters, currentNode.getIndizes());

            // Found an index matching a filter parameter
            if (pair != null)
            {
                remainingParameters.remove(pair.getFirst());
                Object filterForValue = pair.getFirst().getFilterForValue();
                FilterParamIndexBase index = pair.getSecond();
                treePathInfo.add(index, filterForValue);
                addToIndex(index, filterForValue, treePathInfo);
                return;
            }
        }
        finally
        {
            currentNode.getNodeRWLock().readLock().unlock();
        }

        // An index for any of the filter parameters was not found, create one
        currentNode.getNodeRWLock().writeLock().lock();
        try
        {
            pair = IndexHelper.findIndex(remainingParameters, currentNode.getIndizes());

            // Attempt to find an index again this time under a write lock
            if (pair != null)
            {
                remainingParameters.remove(pair.getFirst());
                Object filterForValue = pair.getFirst().getFilterForValue();
                FilterParamIndexBase index = pair.getSecond();
                treePathInfo.add(index, filterForValue);
                addToIndex(index, filterForValue, treePathInfo);
                return;
            }

            // No index found that matches any parameters, create a new one
            // Pick the next parameter for an index
            FilterValueSetParam parameterPickedForIndex = remainingParameters.first();
            remainingParameters.remove(parameterPickedForIndex);

            FilterParamIndexBase index = IndexFactory.createIndex(eventType, parameterPickedForIndex.getPropertyName(),
                    parameterPickedForIndex.getFilterOperator());

            currentNode.getIndizes().add(index);
            treePathInfo.add(index, parameterPickedForIndex.getFilterForValue());
            addToIndex(index, parameterPickedForIndex.getFilterForValue(), treePathInfo);
        }
        finally
        {
            currentNode.getNodeRWLock().writeLock().unlock();
        }
    }

    // Remove an filterCallback from the current node, return true if the node is the node is empty now
    private boolean removeFromNode(FilterHandleSetNode currentNode,
                                   IndexTreePath treePathInfo)
    {
        Pair<FilterParamIndexBase, Object> nextPair = treePathInfo.removeFirst();

        // No remaining filter parameters
        if (nextPair == null)
        {
            currentNode.getNodeRWLock().writeLock().lock();

            try
            {
                boolean isRemoved = currentNode.remove(filterCallback);
                boolean isEmpty = currentNode.isEmpty();

                if (!isRemoved)
                {
                    log.warn(".removeFromNode (" + currentThreadId + ") Could not find the filterCallback to be removed within the supplied node , node=" +
                            currentNode + "  filterCallback=" + filterCallback);
                }

                return isEmpty;
            }
            finally
            {
                currentNode.getNodeRWLock().writeLock().unlock();
            }
        }

        // Remove from index
        FilterParamIndexBase nextIndex = nextPair.getFirst();
        Object filteredForValue = nextPair.getSecond();

        currentNode.getNodeRWLock().writeLock().lock();
        try
        {
            boolean isEmpty = removeFromIndex(nextIndex, treePathInfo, filteredForValue);

            if (!isEmpty)
            {
                return false;
            }

            // Remove the index if the index is now empty
            if (nextIndex.size() == 0)
            {
                boolean isRemoved = currentNode.remove(nextIndex);

                if (!isRemoved)
                {
                    log.warn(".removeFromNode (" + currentThreadId + ") Could not find the index in index list for removal, index=" +
                            nextIndex.toString() + "  filterCallback=" + filterCallback);
                    return false;
                }
            }

            return currentNode.isEmpty();
        }
        finally
        {
            currentNode.getNodeRWLock().writeLock().unlock();
        }
    }

    // Remove filterCallback from index, returning true if index empty after removal
    private boolean removeFromIndex(FilterParamIndexBase index,
                                    IndexTreePath treePathInfo,
                                    Object filterForValue)
    {
        index.getReadWriteLock().writeLock().lock();
        try
        {
            EventEvaluator eventEvaluator = index.get(filterForValue);

            if (eventEvaluator == null)
            {
                log.warn(".removeFromIndex (" + currentThreadId + ") Could not find the filterCallback value in index, index=" +
                        index.toString() + "  value=" + filterForValue.toString() + "  filterCallback=" + filterCallback);
                return false;
            }

            if (eventEvaluator instanceof FilterHandleSetNode)
            {
                FilterHandleSetNode node = (FilterHandleSetNode) eventEvaluator;
                boolean isEmpty = removeFromNode(node, treePathInfo);

                if (isEmpty)
                {
                    // Since we are holding a write lock to this index, there should not be a chance that
                    // another thread had been adding anything to this FilterHandleSetNode
                    index.remove(filterForValue);
                }
                int size = index.size();

                return (size == 0);
            }

            FilterParamIndexBase nextIndex = (FilterParamIndexBase) eventEvaluator;
            Pair<FilterParamIndexBase, Object> nextPair = treePathInfo.removeFirst();

            if (nextPair == null)
            {
                log.fatal(".removeFromIndex Expected an inner index to this index, this=" + this.toString());
                assert false;
                return false;
            }

            if (nextPair.getFirst() != nextIndex)
            {
                log.fatal(".removeFromIndex Expected an index for filterCallback that differs from the found index, this=" + this.toString() +
                        "  expected=" + nextPair.getFirst());
                assert false;
                return false;
            }

            Object nextExpressionValue = nextPair.getSecond();

            boolean isEmpty = removeFromIndex(nextPair.getFirst(), treePathInfo, nextExpressionValue);

            if (isEmpty)
            {
                // Since we are holding a write lock to this index, there should not be a chance that
                // another thread had been adding anything to this FilterHandleSetNode
                index.remove(filterForValue);
            }
            int size = index.size();

            return (size == 0);
        }
        finally
        {
            index.getReadWriteLock().writeLock().unlock();
        }
    }

     /**
     * Add to an index the value to filter for.
     * @param index is the index to add to
     * @param filterForValue is the filter parameter value to add
     * @param treePathInfo is the specification to fill on where is was added
     */
    private void addToIndex(FilterParamIndexBase index,
                            Object filterForValue,
                            IndexTreePath treePathInfo)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".addToIndex (" + currentThreadId + ") Adding to index " +
                      index.toString() +
                      "  expressionValue=" + filterForValue);
        }

        index.getReadWriteLock().readLock().lock();
        EventEvaluator eventEvaluator;
        try
        {
            eventEvaluator = index.get(filterForValue);

            // The filter parameter value already existed in bean, add and release locks
            if (eventEvaluator != null)
            {
                boolean added = addToEvaluator(eventEvaluator, treePathInfo);
                if (added)
                {
                    return;
                }
            }
        }
        finally
        {
            index.getReadWriteLock().readLock().unlock();
        }

        // new filter parameter value, need a write lock
        index.getReadWriteLock().writeLock().lock();
        try
        {
            eventEvaluator = index.get(filterForValue);

            // It may exist now since another thread could have added the entry
            if (eventEvaluator != null)
            {
                boolean added = addToEvaluator(eventEvaluator, treePathInfo);
                if (added)
                {
                    return;
                }

                // The found eventEvaluator must be converted to a new FilterHandleSetNode
                FilterParamIndexBase nextIndex = (FilterParamIndexBase) eventEvaluator;
                FilterHandleSetNode newNode = new FilterHandleSetNode();
                newNode.add(nextIndex);
                index.put(filterForValue, newNode);
                addToNode(newNode, treePathInfo);

                return;
            }

            // The index does not currently have this filterCallback value,
            // if there are no remaining parameters, create a node
            if (remainingParameters.isEmpty())
            {
                FilterHandleSetNode node = new FilterHandleSetNode();
                addToNode(node, treePathInfo);
                index.put(filterForValue, node);
                return;
            }

            // If there are remaining parameters, create a new index for the next parameter
            FilterValueSetParam parameterPickedForIndex = remainingParameters.first();
            remainingParameters.remove(parameterPickedForIndex);

            FilterParamIndexBase nextIndex = IndexFactory.createIndex(eventType, parameterPickedForIndex.getPropertyName(),
                    parameterPickedForIndex.getFilterOperator());

            index.put(filterForValue, nextIndex);
            treePathInfo.add(nextIndex, parameterPickedForIndex.getFilterForValue());
            addToIndex(nextIndex, parameterPickedForIndex.getFilterForValue(), treePathInfo);
        }
        finally
        {
            index.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Add filter callback to an event evaluator, which could be either an index node or a set node.
     * @param eventEvaluator to add the filterCallback to.
     * @param treePathInfo is for holding the information on where the add occured
     * @return boolean indicating if the eventEvaluator was successfully added
     */
    private boolean addToEvaluator(EventEvaluator eventEvaluator, IndexTreePath treePathInfo)
    {
        if (eventEvaluator instanceof FilterHandleSetNode)
        {
            FilterHandleSetNode node = (FilterHandleSetNode) eventEvaluator;
            addToNode(node, treePathInfo);
            return true;
        }

        // Check if the next index matches any of the remaining filterCallback parameters
        FilterParamIndexBase nextIndex = (FilterParamIndexBase) eventEvaluator;

        FilterValueSetParam parameter = IndexHelper.findParameter(remainingParameters, nextIndex);
        if (parameter != null)
        {
            remainingParameters.remove(parameter);
            treePathInfo.add(nextIndex, parameter.getFilterForValue());
            addToIndex(nextIndex, parameter.getFilterForValue(), treePathInfo);
            return true;
        }

        // This eventEvaluator does not work with any of the remaining filter parameters
        return false;
    }

    /**
     * Copy the parameter list - this also sorts the parameter list.
     * @param parameters is a list of filter parameters
     * @return sorted set of filter parameters
     */
    protected static SortedSet<FilterValueSetParam> copySortParameters(List<FilterValueSetParam> parameters)
    {
        SortedSet<FilterValueSetParam> copy = new TreeSet<FilterValueSetParam>(new FilterValueSetParamComparator());

        for (FilterValueSetParam parameter : parameters)
        {
            copy.add(parameter);
        }

        if (copy.size() != parameters.size())
        {
            throw new IllegalArgumentException("Filter parameters not unique by property name and filter operator");
        }
        return copy;
    }

    private String printRemainingParameters()
    {
        StringBuilder buffer = new StringBuilder();

        int count = 0;
        for (FilterValueSetParam parameter : remainingParameters)
        {
            buffer.append("  param(").append(count).append(')');
            buffer.append(" property=").append(parameter.getPropertyName());
            buffer.append(" operator=").append(parameter.getFilterOperator());
            buffer.append(" value=").append(parameter.getFilterForValue());
            count++;
        }

        return buffer.toString();
    }

    private static final Log log = LogFactory.getLog(IndexTreeBuilder.class);
}
