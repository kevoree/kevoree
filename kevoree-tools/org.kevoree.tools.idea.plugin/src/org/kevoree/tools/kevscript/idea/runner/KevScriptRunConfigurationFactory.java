package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by gregory.nain on 17/01/2014.
 */
public class KevScriptRunConfigurationFactory extends ConfigurationFactory {

    protected KevScriptRunConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new KevScriptRunConfiguration(project, this, "Default KevScript Runner");
    }
}
