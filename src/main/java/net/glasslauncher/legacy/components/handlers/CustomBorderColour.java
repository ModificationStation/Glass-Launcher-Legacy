package net.glasslauncher.legacy.components.handlers;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

public class CustomBorderColour implements TableCellRenderer {
    private final TableCellRenderer renderer;
    private Border border;

    public CustomBorderColour(TableCellRenderer renderer, Color sideColour, Color topBottomColour){
        this.renderer = renderer;
        border = BorderFactory.createCompoundBorder();
        border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(1, 0, 1, 0, topBottomColour));
        border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0, 1, 0, 1, sideColour));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JComponent result = (JComponent) renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        result.setBorder(border);
        return result;
    }
}
