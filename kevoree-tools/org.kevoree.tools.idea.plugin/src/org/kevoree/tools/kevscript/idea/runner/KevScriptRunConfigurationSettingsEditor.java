package org.kevoree.tools.kevscript.idea.runner;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by gregory.nain on 17/01/2014.
 */
public class KevScriptRunConfigurationSettingsEditor extends SettingsEditor<KevScriptRunConfiguration> {

    private JLabel kevScriptFileLocation_lbl = new JLabel("KevScript file:");
    private JTextField kevScriptFileLocation_txt = new JTextField();

    @Override
    protected void resetEditorFrom(KevScriptRunConfiguration kevScriptRunConfiguration) {

        if(kevScriptRunConfiguration.getConfigurationModule() != null) {
            if(kevScriptRunConfiguration.getConfigurationModule().getModule() != null) {
                Notifications.Bus.notify(new Notification("KevPlugin", "KevDebug", "ModuleName:" + kevScriptRunConfiguration.getConfigurationModule().getModule().getName(), NotificationType.INFORMATION));
                if(kevScriptRunConfiguration.getConfigurationModule().getModule().getModuleFile()!= null) {
                    Notifications.Bus.notify(new Notification("KevPlugin", "KevDebug", "ModuleFile:" + kevScriptRunConfiguration.getConfigurationModule().getModule().getModuleFile(), NotificationType.INFORMATION));
                    if(kevScriptRunConfiguration.getConfigurationModule().getModule().getModuleFile().findFileByRelativePath("src/main/kevs/main.kevs") != null) {
                        Notifications.Bus.notify(new Notification("KevPlugin", "KevDebug", "KevsFile:" + kevScriptRunConfiguration.getConfigurationModule().getModule().getModuleFile().findFileByRelativePath("src/main/kevs/main.kevs"), NotificationType.INFORMATION));
                        kevScriptRunConfiguration.kevsFile = kevScriptRunConfiguration.getConfigurationModule().getModule().getModuleFile().findFileByRelativePath("src/main/kevs/main.kevs");
                    }
                }
            }
        }

        if(kevScriptRunConfiguration.kevsFile != null) {
            kevScriptFileLocation_txt.setText(kevScriptRunConfiguration.kevsFile.getCanonicalPath());
        }
    }

    @Override
    protected void applyEditorTo(KevScriptRunConfiguration kevScriptRunConfiguration) throws ConfigurationException {

        if(kevScriptRunConfiguration.getConfigurationModule() != null) {
            if(kevScriptRunConfiguration.getConfigurationModule().getModule() != null) {
                Notifications.Bus.notify(new Notification("KevPlugin", "KevDebug", "ModuleName:" + kevScriptRunConfiguration.getConfigurationModule().getModule().getName(), NotificationType.INFORMATION));
                if(kevScriptRunConfiguration.getConfigurationModule().getModule().getModuleFile()!= null) {
                    Notifications.Bus.notify(new Notification("KevPlugin", "KevDebug", "ModuleFile:" + kevScriptRunConfiguration.getConfigurationModule().getModule().getModuleFile(), NotificationType.INFORMATION));
                    if(kevScriptRunConfiguration.getConfigurationModule().getModule().getModuleFile().findFileByRelativePath("src/main/kevs/main.kevs") != null) {
                        Notifications.Bus.notify(new Notification("KevPlugin", "KevDebug", "KevsFile:" + kevScriptRunConfiguration.getConfigurationModule().getModule().getModuleFile().findFileByRelativePath("src/main/kevs/main.kevs"), NotificationType.INFORMATION));
                        kevScriptRunConfiguration.kevsFile = kevScriptRunConfiguration.getConfigurationModule().getModule().getModuleFile().findFileByRelativePath("src/main/kevs/main.kevs");
                    }
                }
            }
        }


        //kevScriptRunConfiguration.kevsFile = kevScriptRunConfiguration.getConfigurationModule().getModule().getModuleFile().findFileByRelativePath( kevScriptFileLocation_txt.getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new FlowLayout());
        mainPanel.add(kevScriptFileLocation_lbl);
        mainPanel.add(kevScriptFileLocation_txt);
        return mainPanel;
    }
}
