package org.kevoree.tools.ui.kevscript;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 30/11/2013
 * Time: 13:50
 */
public class Tester {

    public static void main(String[] args){
        JFrame frame = new JFrame("KevScriptEditor");
        frame.setSize(1024,768);
        KevScriptEditorPanel editor = new KevScriptEditorPanel();
        frame.add(editor);
        frame.setVisible(true);
    }

}
