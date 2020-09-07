package net.glasslauncher.legacy.components;

import net.glasslauncher.repo.api.mod.RepoReader;
import net.glasslauncher.repo.api.mod.jsonobj.ModPreview;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import java.awt.Component;
import java.util.List;

public class ModRepoList extends JList<ModPreview> {
    // Fix for the renderer being called twice every click for some reason.
    private String lastRendered = "";

    public ModRepoList(ModDetailsPanel panel) {

        this.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                ModPreview mod = (ModPreview) value;
                setText(mod.getName());
                if (isSelected && !lastRendered.equals(mod.getId())) {
                    setBackground(getBackground().darker());
                    try {
                        panel.setMod(RepoReader.getMod(mod.getId()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    lastRendered = mod.getId();
                }
                return c;
            }

        });
    }

    public void refresh(List<ModPreview> repoMods) {
        DefaultListModel<ModPreview> repoModsModel = new DefaultListModel<>();
        for (ModPreview repoMod : repoMods) {
            repoModsModel.add(0, repoMod);
        }
        setModel(repoModsModel);

    }

}
