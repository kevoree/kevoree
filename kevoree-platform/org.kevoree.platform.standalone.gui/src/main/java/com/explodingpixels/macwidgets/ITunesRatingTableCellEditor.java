package com.explodingpixels.macwidgets;

import com.explodingpixels.data.Rating;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.EventObject;

/**
 * This table cell editor allows users to a adjust a Rating dragging a mouse over the editor area.
 * @author Paul Connolly paulcconnolly gmail
 */
public class ITunesRatingTableCellEditor
        extends AbstractCellEditor
        implements TableCellEditor {

    private JComponent component = new RatingStarEditorPanel();


    public Object getCellEditorValue() {
        int value = ((RatingStarEditorPanel)component).getLevel();
        return Rating.getRating(value * 20);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        table.addMouseListener((RatingStarEditorPanel)component);
        table.addMouseMotionListener((RatingStarEditorPanel)component);
        return component;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        return super.isCellEditable(e);
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return super.shouldSelectCell(anEvent);
    }
}
