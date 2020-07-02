package net.glasslauncher.legacy.components;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.TransferHandler;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

class DragDropHandler extends TransferHandler {
    DragDropList list;

    DragDropHandler(DragDropList list) {
        this.list = list;
    }

    public boolean canImport(TransferSupport support) {
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false;
        }
        JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
        return dl.getIndex() != -1;
    }

    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        Transferable transferable = support.getTransferable();
        try {
            int draggedImageIndex = Integer.parseInt((String) transferable.getTransferData(DataFlavor.stringFlavor));

            JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
            DefaultListModel model = list.model;
            Object draggedObject = model.get(draggedImageIndex);
            int dropIndex = dl.getIndex();
            if (model.indexOf(draggedObject) < dropIndex) {
                dropIndex--;
            }
            model.removeElement(draggedObject);
            model.add(dropIndex, draggedObject);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}