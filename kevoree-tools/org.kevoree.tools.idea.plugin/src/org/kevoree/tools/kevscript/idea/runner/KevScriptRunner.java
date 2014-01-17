package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by duke on 16/01/2014.
 */
public class KevScriptRunner extends DefaultProgramRunner {

    @NotNull
    @Override
    public String getRunnerId() {
        return "KevoreeRunner";
    }

    @Override
    public boolean canRun(@NotNull String s, @NotNull RunProfile runProfile) {
        System.out.println("Can Run:: s=" + s + " runProfile:" + runProfile.getClass() + "("+runProfile.getName()+")");
        return runProfile instanceof KevScriptRunProfileState;
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {

        environment.getExecutionId();

        System.out.println("Hey");
        super.execute(environment);
    }

    @Override
    protected RunContentDescriptor doExecute(Project project, RunProfileState runProfileState, RunContentDescriptor runContentDescriptor, ExecutionEnvironment executionEnvironment) throws ExecutionException {

        runProfileState.toString();

        System.out.println("Hey2");
        return super.doExecute(project, runProfileState, runContentDescriptor, executionEnvironment);
    }
}
