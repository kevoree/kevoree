/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core.deploy;

import com.espertech.esper.client.deploy.*;

public interface DeploymentStateService
{
    public String nextDeploymentId();

    public String[] getDeployments();
    public DeploymentInformation getDeployment(String deploymentId);
    public DeploymentInformation[] getAllDeployments();

    public void addUpdateDeployment(DeploymentInformation descriptor);
    public void destroy();
    public void remove(String deploymentId);
}
