package com.rendion.jchrome;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.rendion.jchrome.painters.TabRowPainter;
import com.rendion.jchrome.painters.Theme;
import com.rendion.jchrome.painters.ToolbarPainter;
import com.rendion.util.ImageUtils;
import com.rendion.util.WindowUtils;

/*
* TODO:  do super small tabs
* TODO:  make text fit better on right margin of xp
* TODO:  do a translucent theme for vista/win7
* TODO:  toolbar implementation
* TODO:  add jpanel/toolbar switching for the tab control and basic selection events and methods to get the selection
* TODO:  make toolbar optional
* TODO:  gradient paint the tab background
* TODO:  provide sample code for the jframe on windows including title bar painting on windows
* TODO:  provide a template for making toolbar buttons on all themes
* TODO:  provide a platform detect option for theme selection
* TODO:  tab animation
*
*/
public class JChromeTabbedPane extends JPanel implements WindowFocusListener, MouseListener, MouseMotionListener {
    private final ToolbarPainter toolbarPainter;
    private final TabRowPainter tabRowPainter;
    private final ArrayList<Tab> tabs = new ArrayList<Tab>(100);
    private Font captionFont;
    private boolean hasFocus = true;
    private boolean paintBackground = true;
    private int leftMargin = 25;
    private int rightMargin = 25;
    private Theme theme;

    private JPanel target;


    public JChromeTabbedPane(String themeName, JPanel target) {
        this(new Theme(themeName), target);
    }

    public JChromeTabbedPane(Theme aTheme, JPanel target) {
        this.target = target;
        this.theme = aTheme;

        toolbarPainter = new ToolbarPainter(this);
        tabRowPainter = new TabRowPainter(this);

        captionFont = new Font(Font.DIALOG, Font.PLAIN, theme.TAB_TEXT_SIZE);

        WindowUtils.installJComponentRepainterOnWindowFocusChanged(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }


    public Tab addTab() {
        return this.addTab(null, null);
    }

    public Tab addTab(String text) {
        return this.addTab(null, text);
    }

    public Tab addTab(ImageIcon icon, String caption) {
        final Tab t = new Tab(this) {
            @Override
            public void setSelected(boolean isSelected) {
                super.setSelected(isSelected);
                if (isSelected) {
                    if (target != null) {
                        if (target.getComponentCount() > 0) {
                            target.remove(0);
                        }
                        if (getInternPanel() != null) {
                            target.add(getInternPanel(), BorderLayout.CENTER);
                        }
                        target.repaint();
                        target.revalidate();
                    }
                }

            }
        };

        if (icon != null) {
            t.setIcon(icon);
        }

        if (caption != null) {
            t.setCaption(caption);
        }

        t.setIndex(tabs.size());

        for (Tab tab : tabs) {
            tab.setSelected(false);
        }
        t.setSelected(true);

        tabs.add(t);
        tabRowPainter.addTab(t);
        repaint();
        return t;
    }

    public void closeTab(Tab t) {
        int index = t.getIndex();

        tabs.remove(index);

        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).setIndex(i);

        }

        // make sure theres a selection a tab can be closed without selecting it
        if (t.isSelected()) {
            if (index < tabs.size()) {
                tabs.get(index).setSelected(true);
            } else if (tabs.size() > 0) {
                tabs.get(tabs.size() - 1).setSelected(true);
            }
        }

        tabRowPainter.removeTab(index);

        repaint();

        /*
        * In most cases after remove there will be a close button under the cursor
        * after closing a tab...fire a mouse moved asynch to hilite the new close
        * button if remove was called programatically no harm done
        */

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Point p = MouseInfo.getPointerInfo().getLocation();
                    Point p2 = getLocationOnScreen();
                    mouseMoved(new MouseEvent(JChromeTabbedPane.this, MouseEvent.MOUSE_MOVED, 0, 0, p.x - p2.x, p.y - p2.y, 0, 0,
                            0, false, 0));
                } catch (Exception ignore) {
                }
            }
        });

    }

    public Tab getSelectedTab() {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).isSelected()) return tabs.get(i);
        }

        return null;
    }

    public void setSelectedTab(Tab tab) {
        if (!tab.isSelected()) {
            getSelectedTab().setSelected(false);
            tab.setSelected(true);
            repaint();
        }

    }

    protected void paintComponent(Graphics g) {
        if (paintBackground) {
            setBackground(hasFocus ? theme.BACKGROUND_FOCUSED : theme.BACKGROUND_UNFOCUSED);
        }

        super.paintComponent(g);

        if (theme.HINT_ANTIALIAS) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        if (theme.HINT_FRACTIONAL) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }

        g.setFont(captionFont);

        try {
            tabRowPainter.paint((Graphics2D) g);
            toolbarPainter.paint((Graphics2D) g);
        } catch (Exception e) {

        }


    }

    public void windowGainedFocus(WindowEvent e) {
        hasFocus = true;
        repaint();
    }

    public void windowLostFocus(WindowEvent e) {
        hasFocus = false;
        repaint();
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
    }

    public void mousePressed(MouseEvent e) {
        if (e.getY() > theme.TABROW_HEIGHT + Toolbar.HEIGHT || !hasFocus) {
            return;
        }

        boolean repaint = tabRowPainter.mousePressed(e);

        if (!repaint) {
            repaint = toolbarPainter.mousePressed(e);
        }

        if (repaint) {
            repaint();
        }

    }

    public void mouseMoved(MouseEvent e) {
        if (e.getY() > theme.TABROW_HEIGHT + Toolbar.HEIGHT || !hasFocus) {
            return;
        }

        boolean repaint = tabRowPainter.mouseMoved(e);

        if (!repaint) {
            repaint = toolbarPainter.mouseMoved(e);
        }

        if (repaint) {
            repaint();
        }
    }

    // if somethign is pressed and released CLICK! the resulting
    // action will call repaint returning true in this case just says
    // the event was consumed - DO NOT PAINT
    public void mouseReleased(MouseEvent e) {
        if (!hasFocus) {
            return;
        }

        boolean repaint = tabRowPainter.mouseReleased(e);

        if (!repaint) {
            repaint = toolbarPainter.mouseReleased(e);
        }

    }

    public boolean hasFocus() {
        return hasFocus;
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void setPaintBackground(boolean paintBackground) {
        this.paintBackground = paintBackground;
    }

    public Theme getTheme() {
        return theme;
    }

}