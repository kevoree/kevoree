package com.explodingpixels.macwidgets;

import com.explodingpixels.widgets.PopdownButton;
import com.explodingpixels.widgets.PopupMenuCustomizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * <p>
 * A bar that can contain buttons and pop-down buttons that act on a given {@link com.explodingpixels.macwidgets.SourceList}. This
 * control bar is displayed at the base of the {@code SourceList}. The control bar also has a
 * draggable widget that can control the divider location of a given {@link javax.swing.JSplitPane}.
 * </p>
 * <p>
 * Heres how to create and install an empty {@code SourceListControlBar}:
 * <pre>
 * SourceList sourceList = DSourceListITunes.createSourceList();
 * SourceListControlBar controlBar = new SourceListControlBar();
 * sourceList.installSourceListControlBar(controlBar);
 * </pre>
 * The above code creates a control bar that looks like this:
 * <br><br>
 * <img src="../../../../graphics/SourceListControlBar-empty.png">
 * </p>
 * <p>
 * The following code adds two push buttons and a drop-down button to the control bar:
 * <pre>
 * controlBar.createAndAddPopdownButton(MacIcons.GEAR,
 *         new PopupMenuCustomizer() {
 *             public void customizePopup(JPopupMenu popup) {
 *                 popup.removeAll();
 *                 popup.add(new JMenuItem("Item One"));
 *                 popup.add(new JMenuItem("Item Two"));
 *                 popup.add(new JMenuItem("Item Three"));
 *             }
 *         });
 * </pre>
 * The above code creates a control bar that looks like this:
 * <br><br>
 * <img src="../../../../graphics/SourceListControlBar-buttons.png">
 * </p>
 */
public class SourceListControlBar {

    private static final ImageIcon SPLITTER_HANDLE =
            new ImageIcon(SourceListControlBar.class.getResource(
                    "/com/explodingpixels/macwidgets/images/splitter_handle.png"));

    private ComponentBottomBar fComponentBottomBar = new ComponentBottomBar();

    private JSplitPane fSplitPane;

    private final JLabel fSplitterHandle = new JLabel(SPLITTER_HANDLE);

    private final SplitterHandleMouseMovementHandler fMouseListener =
            new SplitterHandleMouseMovementHandler();

    /**
     * Creates a {@code SourceListControlBar}.
     */
    public SourceListControlBar() {
        init();
    }

    private void init() {
        fComponentBottomBar.addComponentToRight(fSplitterHandle);
        fSplitterHandle.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
    }

    /**
     * Connects the draggable widget in this {@code SourceListControlBar} to the divider of the
     * given {@link javax.swing.JSplitPane}. Thus when the user drags the {@code SourceListControlBar} draggable
     * widget, the given {@code JSplitPane}s divider location will be adjusted.
     *
     * @param splitPane the {@code JSplitPane} to connect the draggable widget to.
     */
    public void installDraggableWidgetOnSplitPane(JSplitPane splitPane) {
        if (splitPane == null) {
            throw new IllegalArgumentException("JSplitPane cannot be null.");
        }

        fSplitPane = splitPane;
        fSplitterHandle.addMouseListener(fMouseListener);
        fSplitterHandle.addMouseMotionListener(fMouseListener);
    }

    /**
     * Gets the user interface component representing this {@code SourceListControlBar}. The
     * returned {@link javax.swing.JComponent} should be added to a container that will be displayed.
     *
     * @return the user interface component representing this {@code SourceListControlBar}.
     */
    public JComponent getComponent() {
        return fComponentBottomBar.getComponent();
    }

    private void addComponent(JComponent component) {
        fComponentBottomBar.addComponentToLeftWithBorder(component);
    }

    /**
     * Add a new pop-down style button. The given {@link com.explodingpixels.widgets.PopupMenuCustomizer} will be called just
     * prior to each showing of the menu.
     *
     * @param icon                the icon to use in the pop-down menu.
     * @param popupMenuCustomizer the {@code PopupMenuCustomizer} to be called just prior to showing
     *                            the menu.
     */
    public void createAndAddPopdownButton(Icon icon, PopupMenuCustomizer popupMenuCustomizer) {
        PopdownButton button = MacButtonFactory.createGradientPopdownButton(
                icon, popupMenuCustomizer);
        initSourceListButton(button.getComponent());
        addComponent(button.getComponent());
    }

    /**
     * Adds a new button with the given icon. The given {@link java.awt.event.ActionListener} will be called when
     * the button is pressed.
     *
     * @param icon           the icon to use for the button.
     * @param actionListener the {@code ActionListener} to call when the button is pressed.
     */
    public void createAndAddButton(Icon icon, ActionListener actionListener) {
        JComponent button = MacButtonFactory.createGradientButton(icon, actionListener);
        initSourceListButton(button);
        addComponent(button);
    }

    private static void initSourceListButton(JComponent component) {
        component.setBorder(BorderFactory.createEmptyBorder());
    }

    /**
     * Hides the resize handle.
     */
    public void hideResizeHandle() {
        fSplitterHandle.setVisible(false);
    }

    // Mouse handler for splitter control widget. /////////////////////////////////////////////////

    private class SplitterHandleMouseMovementHandler extends MouseAdapter
            implements MouseMotionListener {

        private int fDelta;

        @Override
        public void mousePressed(MouseEvent e) {
            MouseEvent convertedEvent =
                    SwingUtilities.convertMouseEvent(fSplitterHandle, e, fSplitPane);

            fDelta = fSplitPane.getDividerLocation() - convertedEvent.getPoint().x;
        }

        // MouseMotionListener implementation /////////////////////////////////////////////////////

        public void mouseDragged(MouseEvent e) {
            MouseEvent convertedEvent =
                    SwingUtilities.convertMouseEvent(fSplitterHandle, e, fSplitPane);
            int newLocation = convertedEvent.getPoint().x + fDelta;
            // bound newLocation between the minimum and maximum divider locations.
            int boundedNewLocation = Math.max(fSplitPane.getMinimumDividerLocation(),
                    Math.min(newLocation, fSplitPane.getMaximumDividerLocation()));
            fSplitPane.setDividerLocation(boundedNewLocation);
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

}
