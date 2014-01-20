package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by gregory.nain on 20/01/2014.
 */
public class KevScriptRunState extends CommandLineState {

    ExecutorService executor;

    protected KevScriptRunState(ExecutionEnvironment environment) {
        super(environment);
    }


    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {

        final KevScriptRunnerProcessHandler handler = new KevScriptRunnerProcessHandler(this);

        CompilerManager.getInstance(getEnvironment().getProject()).make((Module) getEnvironment().getDataContext().getData("module"), new CompileStatusNotification() {
            @Override
            public void finished(boolean b, int i, int i2, final CompileContext compileContext) {
                System.out.println("Make finished");

                executor = Executors.newSingleThreadExecutor();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {

                        for (VirtualFile f : compileContext.getAllOutputDirectories()) {
                            handler.notifyTextAvailable("Adding components from location:" + f.getPath() + '\n', ProcessOutputTypes.STDOUT);
                            System.out.println(f.getPath());
                        }

                        for(int j = 0; j < 5; j++) {
                            try {
                                handler.notifyTextAvailable("Working hard " + j + '\n', ProcessOutputTypes.STDOUT);
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.destroyProcess();
                    }
                });

            }
        });
        //System.out.println(moduleOutputDir.getPath());
        //System.out.println(moduleOutputDir.exists());
        //Module m = (Module)getEnvironment().getDataContext().getData("module");
        /*
        GlobalSearchScope contentScope = m.getModuleContentScope();
        GlobalSearchScope runtimeScope = m.getModuleRuntimeScope(true);
        String moduleFilePath = m.getModuleFilePath();
        ModuleWithDependenciesScope moduleScope = (ModuleWithDependenciesScope)m.getModuleScope();

        getEnvironment().getExecutionId();
        //getConfigurationModule().getModule().getModuleRuntimeScope(true);
        */

        return handler;
    }
}
