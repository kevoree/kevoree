package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by duke on 16/01/2014.
 */
public class KevScriptConfiguration extends ModuleBasedConfiguration<KevScriptModuleBasedConfiguration> {

    public KevScriptConfiguration(String name, Project project, KevScriptRunConfigurationType configurationType) {
        super(name, new KevScriptModuleBasedConfiguration(project), configurationType.getConfigurationFactories()[0]);
    }

    @Override
    public Collection<Module> getValidModules() {
        return null;
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return null;
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return null;
    }
}
