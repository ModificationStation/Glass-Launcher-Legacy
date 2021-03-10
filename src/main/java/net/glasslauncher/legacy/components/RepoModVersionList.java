package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.components.templates.DetailsPanel;
import net.glasslauncher.repo.api.mod.RepoReader;
import net.glasslauncher.repo.api.mod.jsonobj.ModPreview;
import net.glasslauncher.repo.api.mod.jsonobj.Version;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RepoModVersionList extends JList<String> {
    // Fix for the renderer being called twice every click for some reason.
    private String lastRendered = "";

    public RepoModVersionList(DetailsPanel panel, String mod) {

        this.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String version = (String) value;
                setText(" " + version);
                setBackground(new Color(52, 52, 52));
                setForeground(new Color(218, 218, 218));
                if (isSelected && !lastRendered.equals(version)) {
                    setBackground(getBackground().darker());
                    try {
                        panel.setMod(RepoReader.getVersion(mod, version));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    lastRendered = version;
                }
                return c;
            }

        });
    }

    public void refresh(List<String> repoMods) {
        DefaultListModel<String> repoModsModel = new DefaultListModel<>();
        for (String repoMod : repoMods) {
            repoModsModel.add(0, repoMod);
        }
        setModel(repoModsModel);
    }
}
