/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core;

/**
 * Marker interface for extension services that provide additional engine or statement-level extensions,
 * such as views backed by a write-behind store.
 */
public interface ExtensionServicesContext
{
    /**
     * Invoked to initialize extension services after engine services initialization.
     */
    public void init();

    /**
     * Invoked to destroy the extension services, when an existing engine is initialized.
     */
    public void destroy();
    
    public boolean isHAEnabled();
}
