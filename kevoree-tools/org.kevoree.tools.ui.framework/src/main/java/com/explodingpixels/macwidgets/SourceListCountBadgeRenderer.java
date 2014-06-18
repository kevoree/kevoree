package com.explodingpixels.macwidgets;

import java.awt.Color;

/**
 * Renders a rounded rectangle (i.e. a badge) with a given number in the center of the rectangle.
 */
public class SourceListCountBadgeRenderer extends MacBadgeRenderer {

    /**
     * Creates a badge renderer.
     */
    public SourceListCountBadgeRenderer(Color selectedColor, Color activeUnselectedColor,
                                        Color inactiveUnselectedColor, Color textColor) {
    	super (selectedColor, activeUnselectedColor, inactiveUnselectedColor, textColor);
    }
}
