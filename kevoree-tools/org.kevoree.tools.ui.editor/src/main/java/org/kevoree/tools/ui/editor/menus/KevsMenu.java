package org.kevoree.tools.ui.editor.menus;/*
* Author : Gregory Nain (developer.name@uni.lu)
* Date : 08/11/12
* (c) 2012 University of Luxembourg – Interdisciplinary Centre for Security Reliability and Trust (SnT)
* All rights reserved
*/

import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.OpenKevsShell;

import javax.swing.*;

public class KevsMenu extends JMenu {

    private KevoreeUIKernel kernel;

    public KevsMenu(KevoreeUIKernel kernel) {
        super("KevScript");
        this.kernel = kernel;

        add(createKevsOpenEditorItem());
    }

    private JMenuItem createKevsOpenEditorItem() {
        JMenuItem openEditor = new JMenuItem("Open editor");
        OpenKevsShell cmdOpenKevsGUI = new OpenKevsShell();
        cmdOpenKevsGUI.setKernel(kernel);
        openEditor.addActionListener(new CommandActionListener(cmdOpenKevsGUI));
        return openEditor;
    }
}
