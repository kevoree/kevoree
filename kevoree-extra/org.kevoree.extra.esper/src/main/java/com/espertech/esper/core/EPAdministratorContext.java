/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

import com.espertech.esper.antlr.ASTUtil;
import com.espertech.esper.client.*;
import com.espertech.esper.client.soda.*;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.generated.EsperEPL2GrammarParser;
import com.espertech.esper.epl.parse.*;
import com.espertech.esper.epl.spec.*;
import com.espertech.esper.pattern.EvalNode;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EPAdministratorContext
{
    private final EPServicesContext services;
    private final ConfigurationOperations configurationOperations;
    private final SelectClauseStreamSelectorEnum defaultStreamSelector;

    public EPAdministratorContext(EPServicesContext services, ConfigurationOperations configurationOperations, SelectClauseStreamSelectorEnum defaultStreamSelector)
    {
        this.configurationOperations = configurationOperations;
        this.defaultStreamSelector = defaultStreamSelector;
        this.services = services;
    }

    public ConfigurationOperations getConfigurationOperations()
    {
        return configurationOperations;
    }

    public SelectClauseStreamSelectorEnum getDefaultStreamSelector()
    {
        return defaultStreamSelector;
    }

    public EPServicesContext getServices()
    {
        return services;
    }
}