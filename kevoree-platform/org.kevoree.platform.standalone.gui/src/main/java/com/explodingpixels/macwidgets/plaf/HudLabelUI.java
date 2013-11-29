package com.explodingpixels.macwidgets.plaf;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import java.awt.*;

/**
 * Creates a Heads Up Display (HUD) style label, similar to that seen in various iApps (e.g. iPhoto).
 * <br>
 * <img src="../../../../../graphics/HUDLabelUI.png">
 */
public class HudLabelUI extends BasicLabelUI {

    @Override
    protected void installDefaults(JLabel c) {
        super.installDefaults(c);
        HudPaintingUtils.initHudComponent(c);
    }

    @Override
    public void paint(Graphics graphics, JComponent c) {
        HudPaintingUtils.updateGraphicsToPaintDisabledControlIfNecessary((Graphics2D) graphics, c);
        ((Graphics2D) graphics).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(graphics, c);
    }

    @Override
    protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY) {
        super.paintEnabledText(l, g, s, textX, textY);
    }
}
