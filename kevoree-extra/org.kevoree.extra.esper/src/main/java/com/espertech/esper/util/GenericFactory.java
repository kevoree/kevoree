/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.util;

/**
 * Factory for an instance of any type. Employs Class.newInstance to instantiate.
 */
public class GenericFactory<T>
{
    private Class<T> clazz;

    /**
     * Ctor.
     * @param clazz Class of which instace must be created
     */
    public GenericFactory(Class<T> clazz)
    {
        this.clazz = clazz;
    }

    /**
     * Create instance of class.
     * @return instance
     * @throws IllegalAccessException is thrown by Class.newInstance
     * @throws InstantiationException is thrown by Class.newInstance
     */
    public T create() throws IllegalAccessException, InstantiationException
    {
        return clazz.newInstance();
    }
}
