package org.kevoree.library.javase.adaptationsuperviser.modelview;


import javax.swing.*;
import java.awt.*;

import org.kevoree.ContainerRoot;
import org.kevoree.tools.ui.editor.KevoreeEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel that displays the KevoreeModel
 */
public class KevoreeModelViewerPanel  extends JPanel {
    public static final Logger logger = LoggerFactory.getLogger(KevoreeModelViewerPanel.class);

    private KevoreeEditor kevoreeEditor;

    public KevoreeModelViewerPanel(){
        this.setLayout(new BorderLayout());

        kevoreeEditor = new KevoreeEditor();
        this.add(kevoreeEditor.getPanel());

        // customize for viewing  only
//        kevoreeEditor.getPanel().getTypeEditorPanel().setVisible(false);
        kevoreeEditor.getPanel().getSplitPane().setDividerLocation(0);
        kevoreeEditor.getPanel().getSplitPane().setLastDividerLocation(0);
        kevoreeEditor.getPanel().getSplitPane().setOneTouchExpandable(false);
        kevoreeEditor.getPanel().getSplitPane().getLeftComponent().setVisible(false);
    }

    public void updateModel(ContainerRoot newModel) {
        kevoreeEditor.loadModelFromObject(newModel);
    }
}
