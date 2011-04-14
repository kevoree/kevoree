/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client.soda;

import java.io.Serializable;
import java.io.StringWriter;

/**
 * Atom representing an expression for use in match-recognize.
 * <p>
 * Event row regular expressions are organized into a tree-like structure with nodes representing sub-expressions.
 */
public class MatchRecognizeRegExNested extends MatchRecognizeRegEx implements Serializable
{
    private static final long serialVersionUID = 7046630048071556077L;
    private MatchRecogizePatternElementType type;

    /**
     * Ctor.
     */
    public MatchRecognizeRegExNested() {
    }

    /**
     * Ctor.
     * @param type multiplicity
     */
    public MatchRecognizeRegExNested(MatchRecogizePatternElementType type) {
        this.type = type;
    }

    /**
     * Returns multiplicity.
     * @return multiplicity
     */
    public MatchRecogizePatternElementType getType() {
        return type;
    }

    /**
     * Sets multiplicity.
     * @param type multiplicity to set
     */
    public void setType(MatchRecogizePatternElementType type) {
        this.type = type;
    }

    public void writeEPL(StringWriter writer) {
        writer.append("(");
        String delimiter = "";
        for (MatchRecognizeRegEx node : this.getChildren())
        {
            writer.append(delimiter);
            node.writeEPL(writer);
            delimiter = " ";
        }
        writer.append(")");
        writer.append(type.getText());
    }
}