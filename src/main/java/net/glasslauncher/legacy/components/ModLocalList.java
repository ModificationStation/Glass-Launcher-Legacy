package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.jsontemplate.Mod;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

public class ModLocalList extends DragDropList {
    // Fix for the renderer being called twice every click for some reason.
    private String lastRendered = "";

    public ModLocalList(ArrayList<Mod> mods, String instPath, ModDetailsPanel panel) {
        super(mods, instPath);

        this.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Mod mod = (Mod) value;
                setText(" " + mod.getName());
                //setBackground(new Color(52, 52, 52));
                //setForeground(new Color(218, 218, 218));
                if (isSelected && !lastRendered.equals(mod.getFileName())) {
                    setBackground(getBackground().darker());
                    try {
                        panel.setLocalMod(mod);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    lastRendered = mod.getFileName();
                }
                return c;
            }

        });
    }

    public void refresh(List<Mod> repoMods) {
        DefaultListModel<Mod> repoModsModel = new DefaultListModel<>();
        for (Mod repoMod : repoMods) {
            repoModsModel.add(0, repoMod);
        }
        setModel(repoModsModel);

    }

}
