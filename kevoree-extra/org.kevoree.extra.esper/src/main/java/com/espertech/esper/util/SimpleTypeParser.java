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
 * Parser of a String input to an object.
 */
public interface SimpleTypeParser
{
    /**
     * Parses the text and returns an object value.
     * @param text to parse
     * @return object value
     */
    public Object parse(String text);
}
