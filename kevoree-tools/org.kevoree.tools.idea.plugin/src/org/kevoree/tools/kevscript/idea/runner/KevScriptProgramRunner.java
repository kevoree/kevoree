package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by gregory.nain on 20/01/2014.
 */
public class KevScriptProgramRunner extends GenericProgramRunner {
    @Nullable
    @Override
    protected RunContentDescriptor doExecute(Project project, RunProfileState runProfileState, RunContentDescriptor runContentDescriptor, ExecutionEnvironment executionEnvironment) throws ExecutionException {
        //getEnvironment().getContentToReuse().getExecutionConsole()
        System.out.println("Wait 3!");
        return null;
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
        System.out.println("Wait !");
        super.execute(environment);
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment env, @Nullable Callback callback) throws ExecutionException {
        System.out.println("Wait 2!");
        super.execute(env, callback);
    }

    @NotNull
    @Override
    public String getRunnerId() {
        return "KevScript Runner";
    }

    @Override
    public boolean canRun(@NotNull String s, @NotNull RunProfile runProfile) {
        System.out.println("Can execute: " + s + " -> " + runProfile.getClass() + "("+ runProfile.toString()+")");
        return runProfile instanceof KevScriptRunConfiguration;
    }
}
