/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.plan;

import java.util.Arrays;

/**
 * Specifies an index to build as part of an overall query plan.
 */
public class QueryPlanIndex
{
    private String[][] indexProps;
    private Class[][] optCoercionTypes;

    /**
     * Ctor.
     * @param indexProps - array of property names with the first dimension suplying the number of
     * distinct indexes. The second dimension can be empty and indicates a full table scan.
     * @param optCoercionTypes - array of coercion types for each index, or null entry for no coercion required
     */
    public QueryPlanIndex(String[][] indexProps, Class[][] optCoercionTypes)
    {
        if ((indexProps == null) || (indexProps.length == 0) || (optCoercionTypes == null))
        {
            throw new IllegalArgumentException("Null or empty index properites or coercion types-per-index parameter is supplied, expecting at least one entry");
        }
        this.indexProps = indexProps;
        this.optCoercionTypes = optCoercionTypes;
    }

    /**
     * Returns property names of all indexes.
     * @return property names array
     */
    public String[][] getIndexProps()
    {
        return indexProps;
    }

    /**
     * Returns property names of all indexes.
     * @return property names array
     */
    public Class[][] getCoercionTypesPerIndex()
    {
        return optCoercionTypes;
    }

    /**
     * Find a matching index for the property names supplied.
     * @param indexFields - property names to search for
     * @return -1 if not found, or offset within indexes if found
     */
    protected int getIndexNum(String[] indexFields)
    {
        // Shallow compare, considers unsorted column names
        for (int i = 0; i < indexProps.length; i++)
        {
            if (Arrays.equals(indexFields, indexProps[i]))
            {
                return i;
            }
        }

        // Deep compare, consider sorted column names
        String[] copyIndexFields = new String[indexFields.length];
        System.arraycopy(indexFields, 0, copyIndexFields, 0, indexFields.length);
        Arrays.sort(copyIndexFields);

        for (int i = 0; i < indexProps.length; i++)
        {
            String[] copyIndexProps = new String[indexProps[i].length];
            System.arraycopy(indexProps[i], 0, copyIndexProps, 0, copyIndexProps.length);
            Arrays.sort(copyIndexProps);
            
            if (Arrays.equals(copyIndexFields, copyIndexProps))
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Add an index specification element.
     * @param indexProperties - list of property names to index
     * @param coercionTypes - list of coercion types if required, or null if no coercion required
     * @return number indicating position of index that was added
     */
    public int addIndex(String[] indexProperties, Class[] coercionTypes)
    {
        int numElements = indexProps.length;
        String[][] newProps = new String[numElements + 1][];
        System.arraycopy(indexProps, 0, newProps, 0, numElements);
        newProps[numElements] = indexProperties;
        indexProps = newProps;

        Class[][] newCoercionTypes = new Class[numElements + 1][];
        System.arraycopy(optCoercionTypes, 0, newCoercionTypes, 0, numElements);
        newCoercionTypes[numElements] = coercionTypes;
        optCoercionTypes = newCoercionTypes;

        return numElements;
    }

    /**
     * Returns a list of coercion types for a given index.
     * @param indexProperties is the index field names
     * @return coercion types, or null if no coercion is required
     */
    public Class[] getCoercionTypes(String[] indexProperties)
    {
        for (int i = 0; i < indexProps.length; i++)
        {
            if (Arrays.deepEquals(indexProps[i], indexProperties))
            {
                return this.optCoercionTypes[i];
            }
        }
        throw new IllegalArgumentException("Index properties not found");
    }

    /**
     * Sets the coercion types for a given index.
     * @param indexProperties is the index property names
     * @param coercionTypes is the coercion types
     */
    public void setCoercionTypes(String[] indexProperties, Class[] coercionTypes)
    {
        boolean found = false;
        for (int i = 0; i < indexProps.length; i++)
        {
            if (Arrays.deepEquals(indexProps[i], indexProperties))
            {
                this.optCoercionTypes[i] = coercionTypes;
                found = true;
            }
        }
        if (!found)
        {
            throw new IllegalArgumentException("Index properties not found");
        }
    }

    public String toString()
    {
        if (indexProps == null)
        {
            return "indexProperties=null";
        }

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < indexProps.length; i++)
        {
            buf.append("indexProperties(").append(i).append(")=").append(Arrays.toString(indexProps[i])).append(' ');
        }

        return buf.toString();
    }

    /**
     * Print index specifications in readable format.
     * @param indexSpecs - define indexes
     * @return readable format of index info
     */
    @SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
    public static String print(QueryPlanIndex[] indexSpecs)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("QueryPlanIndex[]\n");

        for (int i = 0; i < indexSpecs.length; i++)
        {
            buffer.append("  index spec " + i + " : " + indexSpecs[i] + '\n');
        }

        return buffer.toString();
    }
}
