package org.kevoree.tools.ui.editor.menus;/*
* Author : Gregory Nain (developer.name@uni.lu)
* Date : 08/11/12
* (c) 2012 University of Luxembourg – Interdisciplinary Centre for Security Reliability and Trust (SnT)
* All rights reserved
*/

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.DisplayModelTextEditor;
import org.kevoree.tools.ui.editor.command.DisplaySerialMonitor;
import org.kevoree.tools.ui.editor.command.JmDnsLookup;
import org.kevoree.tools.ui.editor.command.OpenKevsShell;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ToolsMenu extends JMenu {

    private KevoreeUIKernel kernel;

    public ToolsMenu(KevoreeUIKernel kernel) {
        super("Tools");
        this.kernel = kernel;

        add(createSerialMonitorItem());
        add(createSourceEditorItem());
        add(createJmdnsLookupItem());
        add(createLoggerItem());
    }

    private JMenuItem createSerialMonitorItem() {
        JMenuItem serialmonitorItem = new JMenuItem("Serial Monitor");
        DisplaySerialMonitor serialMonitor = new DisplaySerialMonitor();
        serialMonitor.setKernel(kernel);
        serialmonitorItem.addActionListener(new CommandActionListener(serialMonitor));
        return serialmonitorItem;
    }

    private JMenuItem createSourceEditorItem() {
        JMenuItem sourceEditorItem = new JMenuItem("Source editor");
        DisplayModelTextEditor sourceEditorCmd = new DisplayModelTextEditor();
        sourceEditorCmd.setKernel(kernel);
        sourceEditorItem.addActionListener(new CommandActionListener(sourceEditorCmd));
        return sourceEditorItem;
    }

    private JMenuItem createJmdnsLookupItem() {
        JMenuItem jmdnsLookup = new JMenuItem("JmDns Lookup");
        JmDnsLookup jmdnsLookupCmd = new JmDnsLookup();
        jmdnsLookupCmd.setKernel(kernel);
        jmdnsLookup.addActionListener(new CommandActionListener(jmdnsLookupCmd));
        return jmdnsLookup;
    }


    private JMenu createLoggerItem() {
        JMenu loggerMenu = new JMenu("Logger");

        ButtonGroup logGroup = new ButtonGroup();

        JMenuItem warn = createLoggerWarnItem();
        JMenuItem debug = createLoggerDebugItem();
        JMenuItem info = createLoggerInfoItem();

        logGroup.add(warn);
        logGroup.add(debug);
        logGroup.add(info);

        loggerMenu.add(warn);
        loggerMenu.add(debug);
        loggerMenu.add(info);

        return loggerMenu;
    }


    private JMenuItem createLoggerWarnItem() {
        JCheckBoxMenuItem warnLevelItem = new JCheckBoxMenuItem("Warn");
        warnLevelItem.setSelected(true);
        warnLevelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                root.setLevel(Level.WARN);
            }
        });
        return warnLevelItem;

    }
    private JMenuItem createLoggerDebugItem() {
        JCheckBoxMenuItem debugLevelItem = new JCheckBoxMenuItem("Debug");
        debugLevelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                root.setLevel(Level.DEBUG);
            }
        });
        return debugLevelItem;
    }

    private JMenuItem createLoggerInfoItem() {

        JCheckBoxMenuItem infoLevelItem = new JCheckBoxMenuItem("Info");
        infoLevelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                root.setLevel(Level.INFO);
            }
        });

        return infoLevelItem;

    }



}
