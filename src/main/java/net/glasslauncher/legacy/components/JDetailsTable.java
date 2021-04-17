package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.RepoModDetailsDialog;
import net.glasslauncher.legacy.components.handlers.CustomBorderColour;
import net.glasslauncher.legacy.components.handlers.RepoModTableModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JDetailsTable extends JTable {
    JDetailsTable self = this;

    public JDetailsTable(Frame parent, RepoModTableModel tableModel, String instName) {
        super(tableModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoCreateRowSorter(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    new RepoModDetailsDialog(parent, ((String) getValueAt(self.rowAtPoint(e.getPoint()), 0)), instName).setVisible(true);
                }
            }
        });
        if (!Config.getLauncherConfig().isThemeDisabled()) {
            setBackground(new Color(52, 52, 52));
            setForeground(new Color(218, 218, 218));
            setIntercellSpacing(new Dimension(0, 0));
            setDefaultRenderer(Object.class, new CustomBorderColour(getDefaultRenderer(Object.class), new Color(46, 46, 46), new Color(38, 38, 38)));
        }
    }

    @Override
    public String getToolTipText(@NotNull MouseEvent event) {
        Point point = event.getPoint();
        int col = columnAtPoint(point);
        int row = rowAtPoint(point);
        if (getValueAt(row, col) != null) {
            return getModel().getValueAt(convertRowIndexToModel(row), col).toString();
        }
        return super.getToolTipText(event);
    }


}
