package com.explodingpixels.painter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;

/**
 * An implemenation of {@link com.explodingpixels.painter.MacWidgetsPainter} that fills the given width and height of a {@link java.awt.Component} with a solid color.
 */
public class RectanglePainter implements MacWidgetsPainter<Component> {

    private final Color fFillColor;

    /**
     * Creates a {@link com.explodingpixels.painter.MacWidgetsPainter} that fills a {@link java.awt.Component} with the given {@link java.awt.Color}.
     * @param fillColor the {@code Color} to fill the {@code Component} with.
     */
    public RectanglePainter(Color fillColor) {
        fFillColor = fillColor;
    }

    public void paint(Graphics2D g, Component object, int width, int height) {
        g.setColor(fFillColor);
        g.fillRect(0, 0, width, height);
    }

}
