/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.type;

/**
 * Factory class for PrimitiveValue for all fundamental Java types.
 */
public final class PrimitiveValueFactory
{
    /**
     * Create a placeholder instance for the primitive Java type passed in.
     * Returns null if the type passed in is not a primitive type.
     * @param type is a fundamental Java
     * @return instance of placeholder representing the type, or null if not a primitive type
     */
    public static PrimitiveValue create(Class type)
    {
        if ((type == Boolean.class) || (type == boolean.class))
        {
            return new BoolValue();
        }
        if ((type == Byte.class) || (type == byte.class))
        {
            return new ByteValue();
        }
        if ((type == Double.class) || (type == double.class))
        {
            return new DoubleValue();
        }
        if ((type == Float.class) || (type == float.class))
        {
            return new FloatValue();
        }
        if ((type == Integer.class) || (type == int.class))
        {
            return new IntValue();
        }
        if ((type == Long.class) || (type == long.class))
        {
            return new LongValue();
        }
        if ((type == Short.class) || (type == short.class))
        {
            return new ShortValue();
        }
        if (type == String.class)
        {
            return new StringValue();
        }

        return null;
    }

}
