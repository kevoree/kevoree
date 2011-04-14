/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.named;

/**
 * Holds property information for joined properties in a lookup.
 */
public class IndexedPropDesc implements Comparable
{
    private String indexPropName;
    private Class coercionType;

    /**
     * Ctor.
     * @param indexPropName is the property name of the indexed field
     * @param coercionType is the type to coerce to
     */
    public IndexedPropDesc(String indexPropName, Class coercionType)
    {
        this.indexPropName = indexPropName;
        this.coercionType = coercionType;
    }

    /**
     * Returns the property name of the indexed field.
     * @return property name of indexed field
     */
    public String getIndexPropName()
    {
        return indexPropName;
    }

    /**
     * Returns the coercion type of key to index field.
     * @return type to coerce to
     */
    public Class getCoercionType()
    {
        return coercionType;
    }

    /**
     * Returns the index property names given an array of descriptors.
     * @param descList descriptors of joined properties
     * @return array of index property names
     */
    public static String[] getIndexProperties(IndexedPropDesc[] descList)
    {
        String[] result = new String[descList.length];
        int count = 0;
        for (IndexedPropDesc desc : descList)
        {
            result[count++] = desc.getIndexPropName();
        }
        return result;
    }

    /**
     * Returns the key coercion types.
     * @param descList a list of descriptors
     * @return key coercion types
     */
    public static Class[] getCoercionTypes(IndexedPropDesc[] descList)
    {
        Class[] result = new Class[descList.length];
        int count = 0;
        for (IndexedPropDesc desc : descList)
        {
            result[count++] = desc.getCoercionType();
        }
        return result;
    }

    public int compareTo(Object o)
    {
        IndexedPropDesc other = (IndexedPropDesc) o;
        return indexPropName.compareTo(other.getIndexPropName());
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

        IndexedPropDesc that = (IndexedPropDesc) o;

        if (!coercionType.equals(that.coercionType))
        {
            return false;
        }
        if (!indexPropName.equals(that.indexPropName))
        {
            return false;
        }
        return true;
    }

    public int hashCode()
    {
        int result;
        result = indexPropName.hashCode();
        result = 31 * result + coercionType.hashCode();
        return result;
    }
}
