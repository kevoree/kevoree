package com.explodingpixels.widgets;

import javax.swing.Icon;

/**
 * An interface for provider's of {@link javax.swing.Icon}s.
 */
public interface IconProvider {

    /**
     * Gets the {@link javax.swing.Icon} for this element.
     *
     * @return the {@code Icon} for this element.
     */
    Icon getIcon();

}
