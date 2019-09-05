package net.glass.glassl;


import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;

class DragListener implements DragSourceListener, DragGestureListener {
    DragDropList list;

    DragSource ds = new DragSource();

    DragListener(DragDropList list) {
        this.list = list;
        DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer(list,
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
        if (dsde.getDropSuccess()) {
            System.out.println("Succeeded");
        } else {
            System.out.println("Failed");
        }
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }
}