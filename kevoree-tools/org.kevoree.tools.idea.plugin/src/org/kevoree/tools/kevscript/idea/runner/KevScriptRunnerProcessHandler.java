package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.sun.javafx.binding.StringFormatter;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT;

/**
 * Created by gregory.nain on 20/01/2014.
 */
public class KevScriptRunnerProcessHandler extends ProcessHandler {

    public KevScriptRunnerProcessHandler(final KevScriptRunState state) {

    }


    @Override
    protected void destroyProcessImpl() {
    }

    @Override
    protected void detachProcessImpl() {

    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Nullable
    @Override
    public OutputStream getProcessInput() {

        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                System.out.println(b);
            }
        };
    }
}
