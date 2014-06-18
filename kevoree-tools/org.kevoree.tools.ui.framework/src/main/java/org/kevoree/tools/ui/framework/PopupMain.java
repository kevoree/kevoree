package org.kevoree.tools.ui.framework;

import com.explodingpixels.macwidgets.HudWindow;

import javax.swing.*;

/**
 * Created by duke on 6/18/14.
 */
public class PopupMain {

    public static void main(String[] args){

        HudWindow hud = new HudWindow("Window");
        hud.getJDialog().setSize(300, 350);
        hud.getJDialog().setLocationRelativeTo(null);
        hud.getJDialog().setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        hud.getJDialog().setVisible(true);


        JFrame jf = new JFrame();
        jf.setVisible(true);


    }

}
