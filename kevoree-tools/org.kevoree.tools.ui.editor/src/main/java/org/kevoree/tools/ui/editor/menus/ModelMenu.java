package org.kevoree.tools.ui.editor.menus;/*
* Author : Gregory Nain (developer.name@uni.lu)
* Date : 08/11/12
* (c) 2012 University of Luxembourg – Interdisciplinary Centre for Security Reliability and Trust (SnT)
* All rights reserved
*/

import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.*;

import javax.swing.*;

public class ModelMenu extends JMenu {

    private KevoreeUIKernel kernel;

    public ModelMenu(KevoreeUIKernel kernel) {
        super("Model");
        this.kernel = kernel;

        add(createShowStatsItem());
        add(createClearItem());
        add(createLoadLibraryItem());
        add(createLoadCoreLibraryItem());
        add(createCheckModelItem());
    }

    private JMenuItem createShowStatsItem() {
        JMenuItem statItem = new JMenuItem("ShowStats");
        ShowStatsCommand statCMD = new ShowStatsCommand();
        statCMD.setKernel(kernel);
        statItem.addActionListener(new CommandActionListener(statCMD));
        return statItem;
    }
    private JMenuItem createClearItem() {
        JMenuItem clearModel = new JMenuItem("Clear");
        ClearModelCommand cmdCM = new ClearModelCommand();
        cmdCM.setKernel(kernel);
        clearModel.addActionListener(new CommandActionListener(cmdCM));
        return clearModel;
    }
    private JMenuItem createLoadLibraryItem() {
        JMenuItem mergeLib = new JMenuItem("Load Library");
        LoadNewLibCommandUI cmdLL = new LoadNewLibCommandUI();
        cmdLL.setKernel(kernel);
        mergeLib.addActionListener(new CommandActionListener(cmdLL));
        return mergeLib;
    }

    private JMenu createLoadCoreLibraryItem() {
        JMenu mergelibraries = new JMenu("Load CoreLibrary");

        mergelibraries.add(createLoadCoreLibaryAllItem());
        mergelibraries.add(createLoadCoreLibaryJavaSEItem());
        mergelibraries.add(createLoadCoreLibaryWebServerItem());
        mergelibraries.add(createLoadCoreLibaryArduinoItem());
        mergelibraries.add(createLoadCoreLibarySkyItem());
        mergelibraries.add(createLoadCoreLibaryAndroidItem());

        return mergelibraries;
    }
    private JMenuItem createLoadCoreLibaryAllItem() {
        JMenuItem mergeDefLib0 = new JMenuItem("ALL");
        MergeDefaultLibrary cmdLDEFL0 = new MergeDefaultLibrary(0);
        cmdLDEFL0.setKernel(kernel);
        mergeDefLib0.addActionListener(new CommandActionListener(cmdLDEFL0));
        return mergeDefLib0;
    }
    private JMenuItem createLoadCoreLibaryJavaSEItem() {
        JMenuItem mergeDefLib1 = new JMenuItem("JAVASE");
        MergeDefaultLibrary cmdLDEFL1 = new MergeDefaultLibrary(1);
        cmdLDEFL1.setKernel(kernel);
        mergeDefLib1.addActionListener(new CommandActionListener(cmdLDEFL1));
        return mergeDefLib1;
    }
    private JMenuItem createLoadCoreLibaryWebServerItem() {
        JMenuItem mergeDefLib2 = new JMenuItem("WEBSERVER");
        MergeDefaultLibrary cmdLDEFL2 = new MergeDefaultLibrary(2);
        cmdLDEFL2.setKernel(kernel);
        mergeDefLib2.addActionListener(new CommandActionListener(cmdLDEFL2));
        return mergeDefLib2;
    }
    private JMenuItem createLoadCoreLibaryArduinoItem() {
        JMenuItem mergeDefLib3 = new JMenuItem("ARDUINO");
        MergeDefaultLibrary cmdLDEFL3 = new MergeDefaultLibrary(3);
        cmdLDEFL3.setKernel(kernel);
        mergeDefLib3.addActionListener(new CommandActionListener(cmdLDEFL3));
        return mergeDefLib3;
    }
    private JMenuItem createLoadCoreLibarySkyItem() {
        JMenuItem mergeDefLib4 = new JMenuItem("SKY");
        MergeDefaultLibrary cmdLDEFL4 = new MergeDefaultLibrary(4);
        cmdLDEFL4.setKernel(kernel);
        mergeDefLib4.addActionListener(new CommandActionListener(cmdLDEFL4));
        return mergeDefLib4;
    }
    private JMenuItem createLoadCoreLibaryAndroidItem() {
        JMenuItem mergeDefLib5 = new JMenuItem("ANDROID");
        MergeDefaultLibrary cmdLDEFL5 = new MergeDefaultLibrary(5);
        cmdLDEFL5.setKernel(kernel);
        mergeDefLib5.addActionListener(new CommandActionListener(cmdLDEFL5));
        return mergeDefLib5;
    }

    private JMenuItem createCheckModelItem() {
        JMenuItem checkModel = new JMenuItem("Check");
        CheckCurrentModel cmdCheck = new CheckCurrentModel();
        cmdCheck.setKernel(kernel);
        checkModel.addActionListener(new CommandActionListener(cmdCheck));
        return checkModel;
    }



}
