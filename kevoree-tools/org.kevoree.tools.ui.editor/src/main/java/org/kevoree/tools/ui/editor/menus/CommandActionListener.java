package org.kevoree.tools.ui.editor.menus;/*
* Author : Gregory Nain (developer.name@uni.lu)
* Date : 08/11/12
* (c) 2012 University of Luxembourg – Interdisciplinary Centre for Security Reliability and Trust (SnT)
* All rights reserved
*/

import org.kevoree.tools.ui.editor.command.Command;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CommandActionListener implements ActionListener {

    private Command _command = null;

    public CommandActionListener(Command command) {
        _command = command;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        _command.execute("");
    }
}
