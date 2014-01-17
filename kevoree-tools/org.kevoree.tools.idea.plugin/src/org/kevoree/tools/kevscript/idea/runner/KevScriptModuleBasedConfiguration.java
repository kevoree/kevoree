package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.project.Project;

/**
 * Created by duke on 16/01/2014.
 */
public class KevScriptModuleBasedConfiguration extends RunConfigurationModule {
    public KevScriptModuleBasedConfiguration(Project project) {
        super(project);
    }
}
