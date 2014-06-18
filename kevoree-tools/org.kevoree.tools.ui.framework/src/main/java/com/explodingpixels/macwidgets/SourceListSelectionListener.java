package com.explodingpixels.macwidgets;

/**
 * An interface for listening to {@link com.explodingpixels.macwidgets.SourceListItem} selections.
 */
public interface SourceListSelectionListener {

    /**
     * Called when a {@link com.explodingpixels.macwidgets.SourceListItem} is selected in a {@link com.explodingpixels.macwidgets.SourceList}.
     * @param item the item that was selected.
     */
    void sourceListItemSelected(SourceListItem item);

}
