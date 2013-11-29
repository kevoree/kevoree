package com.explodingpixels.macwidgets;

import com.explodingpixels.painter.Painter;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;

/**
 * An interface to provide a set of {@link com.explodingpixels.painter.Painter}s and colors to use when painting a
 * {@link com.explodingpixels.macwidgets.SourceList}.
 */
public interface SourceListColorScheme {

    /**
     * The {@link com.explodingpixels.painter.Painter} to use for drawing the {@link com.explodingpixels.macwidgets.SourceList} selection when the
     * {@code SourceList} is in the active window and has focus.
     *
     * @return the {@code Painter} to use for painting the selection in an active window where the
     *         {@code SourceList} has focus.
     */
    Painter<Component> getActiveFocusedSelectedItemPainter();

    /**
     * The {@link com.explodingpixels.painter.Painter} to use for drawing the {@link com.explodingpixels.macwidgets.SourceList} selection when the
     * {@code SourceList} is in the active window and does not have focus.
     *
     * @return the {@code Painter} to use for painting the selection in an active window where the
     *         {@code SourceList} does not have focus.
     */
    Painter<Component> getActiveUnfocusedSelectedItemPainter();

    /**
     * The {@link com.explodingpixels.painter.Painter} to use for drawing the {@link com.explodingpixels.macwidgets.SourceList} selection when the
     * {@code SourceList} is in an inactive window.
     *
     * @return the {@code Painter} to use for painting the selection in an inactive window.
     */
    Painter<Component> getInactiveSelectedItemPainter();

    /**
     * The color to draw a {@link com.explodingpixels.macwidgets.SourceList} category with.
     *
     * @return the color to draw a {@code SourceList} category textwith.
     */
    Color getCategoryTextColor();

    /**
     * The color to draw a {@link com.explodingpixels.macwidgets.SourceList} category's shadow with.
     *
     * @return the color to draw a {@code SourceList} category text's shadow with.
     */
    Color getCategoryTextShadowColor();

    /**
     * The color to draw an unselected {@link com.explodingpixels.macwidgets.SourceList} item with.
     *
     * @return the color to draw an unselected {@link com.explodingpixels.macwidgets.SourceList} item with.
     */
    Color getUnselectedItemTextColor();

    /**
     * The color to draw a selected {@link com.explodingpixels.macwidgets.SourceList} item with.
     *
     * @return the color to draw a selected {@link com.explodingpixels.macwidgets.SourceList} item with.
     */
    Color getSelectedItemTextColor();

    /**
     * The color to draw a selected {@link com.explodingpixels.macwidgets.SourceList} item's shadow with.
     *
     * @return the color to draw a selected {@code SourceList} item's shadow with.
     */
    Color getSelectedItemFontShadowColor();

    /**
     * The background color of the {@link com.explodingpixels.macwidgets.SourceList} when it is in an active window.
     *
     * @return the background color of the {@code SourceList} when it is in an active window.
     */
    Color getActiveBackgroundColor();

    /**
     * The background color of the {@link com.explodingpixels.macwidgets.SourceList} when it is in an inactive window.
     *
     * @return the background color of the {@code SourceList} when it is in an inactive window.
     */
    Color getInactiveBackgroundColor();

    /**
     * The icon to use when a {@link com.explodingpixels.macwidgets.SourceList} node is collapsed and unselected.
     *
     * @return the icon to use when a {@code SourceList} node is collapsed and unselected.
     */
    Icon getUnselectedCollapsedIcon();

    /**
     * The icon to use when a {@link com.explodingpixels.macwidgets.SourceList} node is expanded and unselected.
     *
     * @return the icon to use when a {@code SourceList} node is expanded and unselected.
     */
    Icon getUnselectedExpandedIcon();

    /**
     * The icon to use when a {@link com.explodingpixels.macwidgets.SourceList} node is collapsed and selected.
     *
     * @return the icon to use when a {@code SourceList} node is collapsed and selected.
     */
    Icon getSelectedCollapsedIcon();

    /**
     * The icon to use when a {@link com.explodingpixels.macwidgets.SourceList} node is expanded and selected.
     *
     * @return the icon to use when a {@code SourceList} node is expanded and selected.
     */
    Icon getSelectedExpandedIcon();

    /**
     * The color to draw a badge's text with.
     *
     * @return the color to draw a badge's text with.
     */
    Color getBadgeTextColor();

    /**
     * The color to draw a badge's background with when it's corresponding {@link com.explodingpixels.macwidgets.SourceList} item
     * is selected. This color is used regardless of the wheter the parent window is active or
     * inactive.
     *
     * @return the color to draw a badge's background with when it's corresponding
     *         {@code SourceList} item is selected
     */
    Color getSelectedBadgeColor();

    /**
     * The color to draw a badge's background with when it's corresponding {@link com.explodingpixels.macwidgets.SourceList} item
     * is unselected and the {@code SourceList} is in an active window.
     *
     * @return the color to draw a badge's background with when it's corresponding
     *         {@code SourceList} item is unselected and the {@code SourceList} is in an active
     *         window.
     */
    Color getActiveUnselectedBadgeColor();

    /**
     * The color to draw a badge's background with when it's corresponding {@link com.explodingpixels.macwidgets.SourceList} item
     * is unselected and the {@code SourceList} is in an inactive window.
     *
     * @return the color to draw a badge's background with when it's corresponding
     *         {@code SourceList} item is unselected and the {@code SourceList} is in an inactive
     *         window.
     */
    Color getInativeUnselectedBadgeColor();

}
