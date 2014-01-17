package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by gregory.nain on 17/01/2014.
 */
public class KevScriptRunProfileState implements RunProfileState {

    protected ExecutionEnvironment env;

    public KevScriptRunProfileState(ExecutionEnvironment env) {
        this.env = env;
    }

    @Nullable
    @Override
    public ExecutionResult execute(Executor executor, @NotNull ProgramRunner programRunner) throws ExecutionException {

        System.out.println("Executing: " + programRunner.getRunnerId());
        programRunner.execute(env);

        return null;
    }
}
