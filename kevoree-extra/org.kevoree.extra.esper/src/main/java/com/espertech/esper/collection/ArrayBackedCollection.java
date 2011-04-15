/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.collection;

import java.util.Collection;
import java.util.Iterator;

/**
 * A fast collection backed by an array with severe limitations. Allows direct access to the backing array
 * - this must be used with care as old elements could be in the array and the array is only valid until
 *  the number of elements indicated by size.
 * <p>
 * Implements only the add, size and clear methods of the collection interface.
 * <p>
 * When running out of space for the underlying array, allocates a new array of double the size of the
 * current array.
 * <p>
 * Not synchronized and not thread-safe.
 */
public class ArrayBackedCollection<T> implements Collection<T>
{
    private int lastIndex;
    private int currentIndex;
    private Object[] handles;

    /**
     * Ctor.
     * @param currentSize is the initial size of the backing array.
     */
    public ArrayBackedCollection(int currentSize)
    {
        this.lastIndex = currentSize - 1;
        this.currentIndex = 0;
        this.handles = new Object[currentSize];
    }

    public boolean add(T object)
    {
        if (currentIndex <= lastIndex)
        {
            handles[currentIndex++] = object;
            return true;
        }

        // allocate more by duplicating the current size
        int newSize = lastIndex * 2 + 2;
        Object[] newHandles = new Object[newSize];
        System.arraycopy(handles, 0, newHandles, 0, handles.length);
        handles = newHandles;
        lastIndex = newSize - 1;

        // add
        handles[currentIndex++] = object;
        return true;
    }

    public void clear()
    {
        currentIndex = 0;
    }

    public int size()
    {
        return currentIndex;
    }

    /**
     * Returns the backing object array, valid until the current size.
     * <p>
     * Applications must ensure to not read past current size as old elements can be encountered.
     * @return backing array
     */
    public Object[] getArray()
    {
        return handles;
    }

    public boolean isEmpty()
    {
        throw new UnsupportedOperationException();
    }

    public boolean contains(Object o)
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<T> iterator()
    {
        throw new UnsupportedOperationException();
    }

    public Object[] toArray()
    {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    public <T> T[] toArray(T[] a)
    {
        throw new UnsupportedOperationException();
    }
}
