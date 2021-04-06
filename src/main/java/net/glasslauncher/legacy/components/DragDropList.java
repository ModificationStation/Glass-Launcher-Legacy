package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.components.handlers.DragDropHandler;
import net.glasslauncher.legacy.components.handlers.DragListener;
import net.glasslauncher.legacy.jsontemplate.Mod;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DragDropList extends JList<Mod> {
    public DefaultListModel<Mod> model;

    public DragDropList(ArrayList<Mod> list, String instpath) {
        super(new DefaultListModel<>());
        model = (DefaultListModel<Mod>) getModel();
        for (Mod element : list) {
            model.addElement(element);
        }
        setDragEnabled(true);
        setDropMode(DropMode.INSERT);
        setTransferHandler(new DragDropHandler(this, instpath));
        new DragListener(this);

        this.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Mod mod = (Mod) value;
                setText(mod.toString());
                if (mod.isEnabled()) {
                    setBackground(new Color(204, 255, 204));
                } else {
                    setBackground(new Color(255, 230, 230));
                }
                if (isSelected) {
                    setBackground(getBackground().darker());
                }
                return c;
            }

        });

    }
}