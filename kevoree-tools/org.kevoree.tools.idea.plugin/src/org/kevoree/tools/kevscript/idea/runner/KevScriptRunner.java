package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
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
        return "KevscriptRunner";
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile runProfile) {
        if (DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)) {
            return false;
        }
        return executorId.equals(DefaultRunExecutor.EXECUTOR_ID) && runProfile instanceof KevScriptConfiguration;
    }

    @Override
    protected RunContentDescriptor doExecute(Project project, RunProfileState runProfileState, RunContentDescriptor runContentDescriptor, ExecutionEnvironment executionEnvironment) throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();

        ExecutionResult executionResult = runProfileState.execute(executionEnvironment.getExecutor(), this);
        if (executionResult == null) {
            return null;
        }
        final RunContentBuilder contentBuilder = new RunContentBuilder(this, executionResult, executionEnvironment);
        contentBuilder.setEnvironment(executionEnvironment);
        return contentBuilder.showRunContent(runContentDescriptor);
    }
}
