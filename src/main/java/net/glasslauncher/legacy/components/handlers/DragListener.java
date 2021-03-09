package net.glasslauncher.legacy.components.handlers;


import net.glasslauncher.legacy.components.DragDropList;

import java.awt.datatransfer.*;
import java.awt.dnd.*;

public class DragListener implements DragSourceListener, DragGestureListener {
    DragDropList list;

    DragSource ds = new DragSource();

    public DragListener(DragDropList list) {
        this.list = list;
        ds.createDefaultDragGestureRecognizer(list,
                DnDConstants.ACTION_MOVE, this);

    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        StringSelection transferable = new StringSelection(Integer.toString(list.getSelectedIndex()));
        ds.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);
    }

    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }
}