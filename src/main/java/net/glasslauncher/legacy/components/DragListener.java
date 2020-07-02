package net.glasslauncher.legacy.components;


import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

class DragListener implements DragSourceListener, DragGestureListener {
    DragDropList list;

    DragSource ds = new DragSource();

    DragListener(DragDropList list) {
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