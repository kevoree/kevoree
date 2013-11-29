package com.explodingpixels.macwidgets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.BorderLayout;

public class ComponentBottomBar extends TriAreaComponent {

    ComponentBottomBar() {
        super();
        setBackgroundPainter(MacButtonFactory.GRADIENT_BUTTON_IMAGE_PAINTER);
        getComponent().setBorder(BorderFactory.createMatteBorder(1,0,0,0,
                MacButtonFactory.GRADIENT_BUTTON_BORDER_COLOR));
    }

    public void addComponentToLeftWithBorder(JComponent toolToAdd) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createMatteBorder(0,0,0,1,
                MacButtonFactory.GRADIENT_BUTTON_BORDER_COLOR));
        panel.add(toolToAdd, BorderLayout.CENTER);
        super.addComponentToLeft(panel);
    }

    public void addComponentToCenterWithBorder(JComponent toolToAdd) {
        // TODO use matteBorder when on first center item addition.
        // if this is the first component being added, add a line to the left
        //    and right of the component.
        // else add a border just to the right.
        Border matteBorder = getCenterComponentCount() == 0
                ? BorderFactory.createMatteBorder(0,1,0,1,
                MacButtonFactory.GRADIENT_BUTTON_BORDER_COLOR)
                : BorderFactory.createMatteBorder(0,0,0,1,
                MacButtonFactory.GRADIENT_BUTTON_BORDER_COLOR);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createMatteBorder(0,0,0,1,
                MacButtonFactory.GRADIENT_BUTTON_BORDER_COLOR));
        panel.add(toolToAdd, BorderLayout.CENTER);
        super.addComponentToCenter(panel);
    }

    public void addComponentToRightWithBorder(JComponent toolToAdd) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createMatteBorder(0,0,0,1,
                MacButtonFactory.GRADIENT_BUTTON_BORDER_COLOR));
        panel.add(toolToAdd, BorderLayout.CENTER);
        super.addComponentToRight(panel);
    }

}
