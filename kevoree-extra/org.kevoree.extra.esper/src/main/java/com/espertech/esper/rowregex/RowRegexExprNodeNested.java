/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.rowregex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Nested () regular expression in a regex expression tree.
 */
public class RowRegexExprNodeNested extends RowRegexExprNode
{
    private static final Log log = LogFactory.getLog(RowRegexExprNodeNested.class);

    private final RegexNFATypeEnum type;
    private static final long serialVersionUID = -2079284511194587570L;

    /**
     * Ctor.
     * @param type multiplicity and greedy
     */
    public RowRegexExprNodeNested(RegexNFATypeEnum type) {
        this.type = type;
    }

    /**
     * Returns multiplicity and greedy.
     * @return type
     */
    public RegexNFATypeEnum getType() {
        return type;
    }

    public String toExpressionString() {
        return "(" + this.getChildNodes().get(0).toExpressionString() + ")" + type.getOptionalPostfix();
    }
}
