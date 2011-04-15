/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import com.espertech.esper.util.MetaDefItem;

import java.util.Arrays;
import java.io.Serializable;

/**
 * A node number assigned to evaluation nodes in a tree-structure.
 * <p>
 * Represents node numbers as an array of short. Root nodes get an empty array while each level of child
 * node adds an element. New child nodes are obtained from a parent and subsequent child nodes from the last sibling node.
 */
public class EvalNodeNumber implements MetaDefItem, Serializable
{
    private final short[] number;
    private static final long serialVersionUID = -2623267386821650096L;

    /**
     * Constructs a root node number.
     */
    public EvalNodeNumber()
    {
        number = new short[0];
    }

    private EvalNodeNumber(short[] number)
    {
        this.number = number;
    }

    /**
     * Returns the child number.
     * @return child number
     */
    public short getChildNumber()
    {
        return number[number.length - 1];
    }

    /**
     * Returns true for a root node, false for child nodes.
     * @return true if root, false if child node
     */
    public boolean isRoot()
    {
        return number.length == 0;
    }

    /**
     * Returns the parent's node number, of null if this is the root node number.
     * @return parent node number
     */
    public EvalNodeNumber getParentNumber()
    {
        if (isRoot())
        {
            return null;
        }
        short[] num = new short[number.length - 1];
        System.arraycopy(number, 0, num, 0, number.length - 1);
        return new EvalNodeNumber(num);
    }

    /**
     * Returns a new child node number.
     * @return child node number
     */
    public EvalNodeNumber newChildNumber()
    {
        short[] num = new short[number.length + 1];
        System.arraycopy(number, 0, num, 0, number.length);
        num[number.length] = 0;
        return new EvalNodeNumber(num);
    }

    /**
     * Returns a new sibling node number based on the current node. This call is invalid for root nodes.
     * @return sibling node number
     */
    public EvalNodeNumber newSiblingNumber()
    {
        int size = number.length;
        if (size == 0)
        {
            throw new IllegalStateException("Cannot create a new node number for root node");
        }

        short[] num = new short[size];
        System.arraycopy(number, 0, num, 0, size);
        short next = number[size - 1];
        num[size - 1] = ++next;
        return new EvalNodeNumber(num);
    }

    public String toString()
    {
        return Arrays.toString(number);
    }

    /**
     * Returns the node number representation in an array of short.
     * @return node number as short array
     */
    public short[] getNumber()
    {
        return number;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        EvalNodeNumber that = (EvalNodeNumber) o;

        if (!Arrays.equals(number, that.number))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return Arrays.hashCode(number);
    }
}
