/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.lookup;

import java.util.Collection;

/**
 * Holds property information for joined properties in a lookup.
 */
public class JoinedPropDesc implements Comparable
{
    private String indexPropName;
    private Class coercionType;
    private String keyPropName;
    private Integer keyStreamId;

    /**
     * Ctor.
     * @param indexPropName is the property name of the indexed field
     * @param coercionType is the type to coerce to
     * @param keyPropName is the property name of the key field
     * @param keyStreamId is the stream number of the key field
     */
    public JoinedPropDesc(String indexPropName, Class coercionType, String keyPropName, Integer keyStreamId)
    {
        this.indexPropName = indexPropName;
        this.coercionType = coercionType;
        this.keyPropName = keyPropName;
        this.keyStreamId = keyStreamId;
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
     * Returns the property name of the key field.
     * @return property name of key field
     */
    public String getKeyPropName()
    {
        return keyPropName;
    }

    /**
     * Returns the stream id of the key field.
     * @return stream id
     */
    public Integer getKeyStreamId()
    {
        return keyStreamId;
    }

    /**
     * Returns the key stream numbers.
     * @param descList a list of descriptors
     * @return key stream numbers
     */
    public static int[] getKeyStreamNums(Collection<JoinedPropDesc> descList)
    {
        int[] streamIds = new int[descList.size()];
        int count = 0;
        for (JoinedPropDesc desc : descList)
        {
            streamIds[count++] = desc.getKeyStreamId();
        }
        return streamIds;
    }

    /**
     * Returns the key stream numbers.
     * @param descList a list of descriptors
     * @return key stream numbers
     */
    public static int[] getKeyStreamNums(JoinedPropDesc[] descList)
    {
        int[] streamIds = new int[descList.length];
        int count = 0;
        for (JoinedPropDesc desc : descList)
        {
            streamIds[count++] = desc.getKeyStreamId();
        }
        return streamIds;
    }

    /**
     * Returns the key property names.
     * @param descList a list of descriptors
     * @return key property names
     */
    public static String[] getKeyProperties(Collection<JoinedPropDesc> descList)
    {
        String[] result = new String[descList.size()];
        int count = 0;
        for (JoinedPropDesc desc : descList)
        {
            result[count++] = desc.getKeyPropName();
        }
        return result;
    }

    /**
     * Returns the key property names.
     * @param descList a list of descriptors
     * @return key property names
     */
    public static String[] getKeyProperties(JoinedPropDesc[] descList)
    {
        String[] result = new String[descList.length];
        int count = 0;
        for (JoinedPropDesc desc : descList)
        {
            result[count++] = desc.getKeyPropName();
        }
        return result;
    }

    /**
     * Returns the index property names given an array of descriptors.
     * @param descList descriptors of joined properties
     * @return array of index property names
     */
    public static String[] getIndexProperties(JoinedPropDesc[] descList)
    {
        String[] result = new String[descList.length];
        int count = 0;
        for (JoinedPropDesc desc : descList)
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
    public static Class[] getCoercionTypes(Collection<JoinedPropDesc> descList)
    {
        Class[] result = new Class[descList.size()];
        int count = 0;
        for (JoinedPropDesc desc : descList)
        {
            result[count++] = desc.getCoercionType();
        }
        return result;
    }

    /**
     * Returns the key coercion types.
     * @param descList a list of descriptors
     * @return key coercion types
     */
    public static Class[] getCoercionTypes(JoinedPropDesc[] descList)
    {
        Class[] result = new Class[descList.length];
        int count = 0;
        for (JoinedPropDesc desc : descList)
        {
            result[count++] = desc.getCoercionType();
        }
        return result;
    }

    public int compareTo(Object o)
    {
        JoinedPropDesc other = (JoinedPropDesc) o;
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

        JoinedPropDesc that = (JoinedPropDesc) o;

        if (!coercionType.equals(that.coercionType))
        {
            return false;
        }
        if (!indexPropName.equals(that.indexPropName))
        {
            return false;
        }
        if (!keyPropName.equals(that.keyPropName))
        {
            return false;
        }
        if (!keyStreamId.equals(that.keyStreamId))
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
        result = 31 * result + keyPropName.hashCode();
        result = 31 * result + keyStreamId.hashCode();
        return result;
    }
}
