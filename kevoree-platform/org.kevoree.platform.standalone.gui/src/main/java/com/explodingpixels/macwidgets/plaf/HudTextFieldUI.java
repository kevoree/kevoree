package com.explodingpixels.macwidgets.plaf;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Creates a Heads Up Display (HUD) style text field, similar to that seen in various iApps (e.g.
 * iPhoto).
 * <br>
 * <img src="../../../../../graphics/HUDTextFieldUI.png">
 */
public class HudTextFieldUI extends BasicTextFieldUI {

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        JTextComponent textComponent = (JTextComponent) c;

        textComponent.setOpaque(false);
        textComponent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HudPaintingUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(1, 2, 1, 2)));
        textComponent.setBackground(new Color(0, 0, 0, 0));
        textComponent.setForeground(HudPaintingUtils.FONT_COLOR);
        textComponent.setFont(HudPaintingUtils.getHudFont());
        textComponent.setSelectedTextColor(Color.BLACK);
        textComponent.setSelectionColor(HudPaintingUtils.FONT_COLOR);
        textComponent.setCaretColor(HudPaintingUtils.FONT_COLOR);
    }

    @Override
    protected void paintSafely(Graphics graphics) {
        ((Graphics2D) graphics).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintSafely(graphics);
    }
}
