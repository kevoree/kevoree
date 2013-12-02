package org.kevoree.platform.standalone.gui;

import com.explodingpixels.macwidgets.HudWidgetFactory;
import com.explodingpixels.macwidgets.MacUtils;
import com.explodingpixels.macwidgets.UnifiedToolBar;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 01/12/2013
 * Time: 18:01
 */
public class InitFrame extends JFrame {

    public InitFrame() {
        setTitle("Kevoree Platform - Bootstrap");
        MacUtils.makeWindowLeopardStyle(this.getRootPane());
        UnifiedToolBar toolBar = new UnifiedToolBar();


        JTextField name = HudWidgetFactory.createHudTextField("node name");
        //JPanel lay = new JPanel();


        add(name, BorderLayout.CENTER);


    }

}
