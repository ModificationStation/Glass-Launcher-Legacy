package net.glasslauncher.legacy.components;

import javax.swing.*;
import java.util.ArrayList;

public class DragDropList extends JList {
    DefaultListModel model;

    public DragDropList(ArrayList list) {
        super(new DefaultListModel());
        model = (DefaultListModel) getModel();
        for (Object element : list) {
            model.addElement(element);
        }
        setDragEnabled(true);
        setDropMode(DropMode.INSERT);
        setTransferHandler(new DragDropHandler(this));
        new DragListener(this);
    }
}