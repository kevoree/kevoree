/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.view;

/**
 * An empty output condition that is always satisfied.
 */
public class OutputConditionNull implements OutputCondition {

	private static final boolean DO_OUTPUT = true;
	private static final boolean FORCE_UPDATE = false;

	private final OutputCallback outputCallback;

	/**
	 * Ctor.
	 * @param outputCallback is the callback to make once the condition is satisfied
	 */
	public OutputConditionNull(OutputCallback outputCallback)
	{
        if(outputCallback == null)
        {
        	throw new NullPointerException("Output condition requires a non-null callback");
        }
		this.outputCallback = outputCallback;
	}

	public void updateOutputCondition(int newEventsCount, int oldEventsCount) {
		outputCallback.continueOutputProcessing(DO_OUTPUT, FORCE_UPDATE);
	}

}
