package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import org.jetbrains.annotations.NotNull;
import org.kevoree.tools.kevscript.idea.KevIcons;

import javax.swing.*;

/**
 * Created by duke on 16/01/2014.
 */
public class KevScriptRunConfigurationType implements ConfigurationType {
    @Override
    public String getDisplayName() {
        return "KevScript Application";
    }

    @Override
    public String getConfigurationTypeDescription() {
        return "KevScript Application";
    }

    @Override
    public Icon getIcon() {
        return KevIcons.KEVS_ICON_16x16;
    }

    @NotNull
    @Override
    public String getId() {
        return "KevScriptApplicationRunConfiguration";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[0];
    }
}
