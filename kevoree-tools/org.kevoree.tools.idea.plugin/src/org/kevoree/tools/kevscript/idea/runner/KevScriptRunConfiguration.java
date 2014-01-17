package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by gregory.nain on 17/01/2014.
 */
public class KevScriptRunConfiguration extends ModuleBasedConfiguration<KevRunConfigurationModule> {

    public VirtualFile kevsFile;

    protected KevScriptRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(name, new KevRunConfigurationModule(project), factory);

    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new KevScriptRunConfigurationSettingsEditor();
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {

    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        return null;
    }

    @Override
    public Collection<Module> getValidModules() {
        return this.getAllModules();
    }



}
