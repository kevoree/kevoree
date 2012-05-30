package org.kevoree.library.ui.layout;

import com.rendion.jchrome.JChromeTabbedPane;
import com.rendion.jchrome.Tab;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 30/05/12
 * Time: 19:07
 */
public class KevoreeLayout {

    private static KevoreeLayout instance = null;

    public static KevoreeLayout getInstance() {
        if (instance == null) {
            instance = new KevoreeLayout();
        }
        return instance;
    }

    private KevoreeLayout() {

    }

    JFrame frame = null;
    JChromeTabbedPane p = null;
    private JPanel content = new JPanel();

    private void init() {

        content.setLayout(new BorderLayout());

        // SwingUtilities.invokeLater(new Runnable() {
        //   public void run() {
        frame = new JFrame("Kevoree");
        frame.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
        p = new JChromeTabbedPane("osx",content);
        p.setPreferredSize(new Dimension(50, 40));
        p.getSize(p.getPreferredSize());
        if (System.getProperty("os.name").toUpperCase().startsWith("MAC")) {
            p.setPaintBackground(false);
        }

        frame.add(p, BorderLayout.NORTH);
        frame.add(content, BorderLayout.CENTER);
        //frame.setContentPane(p);
        frame.setSize(1024, 768);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
        //  }
        //  });
    }

    private void release() {
        frame.dispose();
        frame = null;
        p = null;
    }

    private HashMap<String, Tab> tabs = new HashMap<String, Tab>();

    public synchronized void displayTab(JComponent c, String key) {
        if (tabs.isEmpty()) {
            init();
        }
        if (p != null) {
            Tab t = p.addTab(key);
            t.setInternPanel(c);
            t.setSelected(true);
            tabs.put(key, t);

        }
    }

    public synchronized void releaseTab(String key) {
        //DO RELEASE
        if (p != null && tabs.containsKey(key)) {
            p.closeTab(tabs.get(key));
        }
        tabs.remove(key);
        //CHECK CLOSE
        if (tabs.isEmpty()) {
            release();
        }
    }

    public static void main(String[] args) {
        getInstance().displayTab(new JLabel("YO"), "YOTab");
        //getInstance().displayTab(new JLabel("ta mere en short"), "MM");
       // getInstance().displayTab(new JLabel("YO33333"), "fucking");
    }

}
