package net.glass.glassl;

import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JList;

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