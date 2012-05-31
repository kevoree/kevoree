package org.kevoree.library.ui.layout;

import com.rendion.jchrome.JChromeTabbedPane;
import com.rendion.jchrome.Tab;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
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
    private JPanel content = null;

    private void init() {

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    content = new JPanel();
                    content.setLayout(new BorderLayout());
                    frame = new JFrame("Kevoree node : "+System.getProperty("node.name"));
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
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void release() {




        content.removeAll();
        frame.remove(content);
        frame.setVisible(false);
        frame.dispose();
        frame = null;
        p = null;
        content = null;



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

        //RELEASE LISTENER
        JComponent subPanel = tabs.get(key).getInternPanel();
        content.remove(subPanel);

        AncestorListener[] listeners = subPanel.getAncestorListeners();
        for (AncestorListener listener : listeners) {
            subPanel.removeAncestorListener(listener);
        }
        VetoableChangeListener[] listeners2 = subPanel.getVetoableChangeListeners();
        for (VetoableChangeListener listener : listeners2) {
            subPanel.removeVetoableChangeListener(listener);
        }
        ComponentListener[] listeners3 = subPanel.getComponentListeners();
        for (ComponentListener listener : listeners3) {
            subPanel.removeComponentListener(listener);
        }
        FocusListener[] listeners4 = subPanel.getFocusListeners();
        for (FocusListener listener : listeners4) {
            subPanel.removeFocusListener(listener);
        }
        HierarchyBoundsListener[] listeners5 = subPanel.getHierarchyBoundsListeners();
        for (HierarchyBoundsListener listener : listeners5) {
            subPanel.removeHierarchyBoundsListener(listener);
        }
        HierarchyListener[] listeners6 = subPanel.getHierarchyListeners();
        for (HierarchyListener listener : listeners6) {
            subPanel.removeHierarchyListener(listener);
        }
        InputMethodListener[] listeners7 = subPanel.getInputMethodListeners();
        for (InputMethodListener listener : listeners7) {
            subPanel.removeInputMethodListener(listener);
        }
        KeyListener[] listeners8 = subPanel.getKeyListeners();
        for (KeyListener listener : listeners8) {
            subPanel.removeKeyListener(listener);
        }
        MouseListener[] listeners9 = subPanel.getMouseListeners();
        for (MouseListener listener : listeners9) {
            subPanel.removeMouseListener(listener);
        }
        MouseMotionListener[] listeners10 = subPanel.getMouseMotionListeners();
        for (MouseMotionListener listener : listeners10) {
            subPanel.removeMouseMotionListener(listener);
        }
        MouseWheelListener[] listeners11 = subPanel.getMouseWheelListeners();
        for (MouseWheelListener listener : listeners11) {
            subPanel.removeMouseWheelListener(listener);
        }
        PropertyChangeListener[] listeners12 = subPanel.getPropertyChangeListeners();
        for (PropertyChangeListener listener : listeners12) {
            subPanel.removePropertyChangeListener(listener);
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
