package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * Created by gregory.nain on 17/01/2014.
 */
public class KevScriptRunnerModuleComponent implements ModuleComponent {
    public KevScriptRunnerModuleComponent(Module module) {
    }

    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "KevScriptRunnerModuleComponent";
    }

    public void projectOpened() {
        // called when project is opened
    }

    public void projectClosed() {
        // called when project is being closed
    }

    public void moduleAdded() {
        // Invoked when the module corresponding to this component instance has been completely
        // loaded and added to the project.
    }
}
