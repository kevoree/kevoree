package com.explodingpixels.macwidgets;

/**
 * An interface for listening for expansion events.
 * It handles expansion events for both {@link com.explodingpixels.macwidgets.SourceListItem} and {@link com.explodingpixels.macwidgets.SourceListCategory}.
 */
public interface SourceListExpansionListener {

    /**
     * Called before expanding a {@link com.explodingpixels.macwidgets.SourceListItem} in a {@link com.explodingpixels.macwidgets.SourceList}.
     * Determines whether an item is allowed to be expanded or not
     * @param item the item that requests to be expanded.
     * @return true if the item is expandable, false otherwise
     */
    boolean shouldExpandSourceListItem(SourceListItem item);

    /**
     * Called when a {@link com.explodingpixels.macwidgets.SourceListItem} is expanded in a {@link com.explodingpixels.macwidgets.SourceList}.
     * The method will only be called if {@link com.explodingpixels.macwidgets.SourceListExpansionListener#shouldExpandSourceListItem(com.explodingpixels.macwidgets.SourceListItem)}
     * returns true.
     * @param item the item that was expanded.
     */
    void sourceListItemExpanded(SourceListItem item);

    /**
     * Called before collapsing a {@link com.explodingpixels.macwidgets.SourceListItem} in a {@link com.explodingpixels.macwidgets.SourceList}.
     * Determines whether an item is allowed to be collapsed or not
     * @param item the item that requests to be collapsed.
     * @return true if the item is collapsable, false otherwise
     */
    boolean shouldCollapseSourceListItem(SourceListItem item);

    /**
     * Called when a {@link com.explodingpixels.macwidgets.SourceListItem} is collapsed in a {@link com.explodingpixels.macwidgets.SourceList}.
     * The method will only be called if {@link com.explodingpixels.macwidgets.SourceListExpansionListener#shouldCollapseSourceListItem(com.explodingpixels.macwidgets.SourceListItem)}
     * returns true.
     * @param item the item that was collapsed.
     */
    void sourceListItemCollapsed(SourceListItem item);

    /**
     * Called before expanding a {@link com.explodingpixels.macwidgets.SourceListCategory} in a {@link com.explodingpixels.macwidgets.SourceList}.
     * Determines whether a category is allowed to be expanded or not
     * @param category the category that requests to be expanded.
     * @return true if the item is expandable, false otherwise
     */
    boolean shouldExpandSourceListCategory(SourceListCategory category);

    /**
     * Called when a {@link com.explodingpixels.macwidgets.SourceListCategory} is expanded in a {@link com.explodingpixels.macwidgets.SourceList}.
     * The method will only be called if {@link com.explodingpixels.macwidgets.SourceListExpansionListener#shouldExpandSourceListCategory(com.explodingpixels.macwidgets.SourceListCategory)}
     * returns true.
     * @param category the category that was expanded.
     */
    void sourceListCategoryExpanded(SourceListCategory category);

    /**
     * Called before collapsing a {@link com.explodingpixels.macwidgets.SourceListCategory} in a {@link com.explodingpixels.macwidgets.SourceList}.
     * Determines whether a category is allowed to be collapsed or not
     * @param category the category that requests to be collapsed.
     * @return true if the item is collapsable, false otherwise
     */
    boolean shouldToCollapseSourceListCategory(SourceListCategory category);

    /**
     * Called when a {@link com.explodingpixels.macwidgets.SourceListCategory} is collapsed in a {@link com.explodingpixels.macwidgets.SourceList}.
     * The method will only be called if {@link com.explodingpixels.macwidgets.SourceListExpansionListener#shouldToCollapseSourceListCategory(com.explodingpixels.macwidgets.SourceListCategory)}
     * returns true.
     * @param category the category that was collapsed.
     */
    void sourceListCategoryCollapsed(SourceListCategory category);
}
