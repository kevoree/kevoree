package org.kevoree.tools.ui.framework.elements;


import javax.swing.*;
import java.awt.*;

public class GroupAnchorPanel extends JPanel {

    public GroupPanel getParentPanel() {
        return parentPanel;
    }

    private GroupPanel parentPanel = null;

    public GroupAnchorPanel(GroupPanel parent){
          parentPanel = parent;
    }

    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setColor(Color.ORANGE);

        GradientPaint grad = new GradientPaint(new Point(0, 0), Color.ORANGE, new Point(0, getHeight()), new Color(150, 150, 150, 220));
        g2.setPaint(grad);
        g2.fillOval(0, 0, 25, 25);

        g2.dispose();
    }

}
