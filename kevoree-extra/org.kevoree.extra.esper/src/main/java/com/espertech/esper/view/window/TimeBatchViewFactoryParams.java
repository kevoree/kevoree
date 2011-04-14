/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.window;

import com.espertech.esper.client.EventType;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.ViewParameterException;

/**
 * Parameters for batch views that provides common data flow parameter parsing.
 */
public class TimeBatchViewFactoryParams {

    /**
     * Keyword for force update, i.e. update if no data.
     */
    protected static final String FORCE_UPDATE_KEYWORD = "force_update";

    /**
     * Keyword for starting eager, i.e. start early.
     */
    protected static final String START_EAGER_KEYWORD = "start_eager";

    /**
     * Event type
     */
    protected EventType eventType;

    /**
     * Number of msec before batch fires (either interval or number of events).
     */
    protected long millisecondsBeforeExpiry;

    /**
     * Indicate whether to output only if there is data, or to keep outputting empty batches.
     */
    protected boolean isForceUpdate;

    /**
     * Indicate whether to output only if there is data, or to keep outputting empty batches.
     */
    protected boolean isStartEager;

    /**
     * Process view expiry parameter
     * @param parameter the parameter to parse
     * @param errorMessage error text
     * @param errorMessage2 error text
     * @throws ViewParameterException if validation failed
     */
	protected void processExpiry(Object parameter, String errorMessage, String errorMessage2) throws ViewParameterException {
        if (!(parameter instanceof Number))
        {
            throw new ViewParameterException(errorMessage);
        }
        else
        {
            Number param = (Number) parameter;
            if (JavaClassHelper.isFloatingPointNumber(param))
            {
            	millisecondsBeforeExpiry = Math.round(1000d * param.doubleValue());
            }
            else
            {
                millisecondsBeforeExpiry = 1000 * param.longValue();
            }
        }

        if (millisecondsBeforeExpiry < 1)
        {
            throw new ViewParameterException(errorMessage2);
        }
	}

	/**
	 * Convert keywords into isForceUpdate and isStartEager members
	 * @param keywords flow control keyword string
	 * @param errorMessage error message
	 * @throws ViewParameterException if parsing failed
	 */
	protected void processKeywords(Object keywords, String errorMessage) throws ViewParameterException {

		if (!(keywords instanceof String))
		{
		    throw new ViewParameterException(errorMessage);
		}

		String[] keyword = ((String) keywords).split(",");
		for (int i = 0; i < keyword.length; i++)
		{
		    String keywordText = keyword[i].toLowerCase().trim();
		    if (keywordText.length() == 0)
		    {
		        continue;
		    }
		    if (keywordText.equals(FORCE_UPDATE_KEYWORD))
		    {
		        isForceUpdate = true;
		    }
		    else if (keywordText.equals(START_EAGER_KEYWORD))
		    {
		        isForceUpdate = true;
		        isStartEager = true;
		    }
		    else
		    {
		        String keywordRange = FORCE_UPDATE_KEYWORD + "," + START_EAGER_KEYWORD;
		        throw new ViewParameterException("Time-length-combination view encountered an invalid keyword '" + keywordText + "', valid control keywords are: " + keywordRange);
		    }
		}
	}
}
