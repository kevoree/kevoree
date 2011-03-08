package org.kevoree.library.gossiper;

import java.util.List;

/**
 * A method for resolving inconsistent object values into a single value.
 * Applications can implement this to provide a method for reconciling conflicts
 * that cannot be resolved simply by the version information.
 * 
 * 
 */
public interface InconsistencyResolver<T> {

    /**
     * Take two different versions of an object and combine them into a single
     * version of the object Implementations must maintain the contract that
     * <ol>
     * <li>
     * {@code resolveConflict([null, null]) == null}</li>
     * <li>
     * if {@code t != null}, then
     * 
     * {@code resolveConflict([null, t]) == resolveConflict([t, null]) == t}</li>
     * 
     * @param items The items to be resolved
     * @return The united object
     */
    public List<T> resolveConflicts(List<T> items);

}
