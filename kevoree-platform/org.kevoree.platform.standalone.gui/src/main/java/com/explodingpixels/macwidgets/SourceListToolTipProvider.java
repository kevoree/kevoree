package com.explodingpixels.macwidgets;

/**
 * An interface that allows implementors to supply the tool tip for a {@link com.explodingpixels.macwidgets.SourceListCategory} or
 * {@link com.explodingpixels.macwidgets.SourceListItem}.
 */
public interface SourceListToolTipProvider {

    /**
     * Gets the tool tip to use for the given {@link com.explodingpixels.macwidgets.SourceListCategory}.
     *
     * @param category the {@code SourceListCategory} to get the tooltip for.
     * @return the tool tip, or null if no tool tip should be shown.
     */
    String getTooltip(SourceListCategory category);

    /**
     * Gets the tool tip to use for the given {@link com.explodingpixels.macwidgets.SourceListItem}.
     *
     * @param item the {@code SourceListItem} to get the tooltip for.
     * @return the tool tip, or null if no tool tip should be shown.
     */
    String getTooltip(SourceListItem item);

}
