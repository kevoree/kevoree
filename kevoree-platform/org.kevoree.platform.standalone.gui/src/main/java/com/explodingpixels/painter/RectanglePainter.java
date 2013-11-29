package com.explodingpixels.painter;

import java.awt.*;

/**
 * An implemenation of {@link com.explodingpixels.painter.Painter} that fills the given width and height of a {@link java.awt.Component} with a solid color.
 */
public class RectanglePainter implements Painter<Component> {

    private final Color fFillColor;

    /**
     * Creates a {@link com.explodingpixels.painter.Painter} that fills a {@link java.awt.Component} with the given {@link java.awt.Color}.
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
