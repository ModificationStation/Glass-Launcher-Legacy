package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.jsontemplate.Mod;
import net.glasslauncher.legacy.jsontemplate.ModCompatInfo;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class CompatList extends JList<Mod> {
    public DefaultListModel<Mod> model;
    private String lastRendered = "";

    public CompatList(CompatibilityDetailsPanel panel) {
        super(new DefaultListModel<>());

        this.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Mod mod = (Mod) value;
                setText(" " + mod.getName());
                if (mod.isEnabled()) {
                    setBackground(new Color(204, 255, 204));
                } else {
                    setBackground(new Color(255, 230, 230));
                }
                if (isSelected) {
                    setBackground(getBackground().darker());
                    if (!lastRendered.equals(mod.getFileName())) {
                        try {
                            panel.setMod(mod);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        lastRendered = mod.getFileName();
                    }
                }
                return c;
            }

        });

    }

    public void refresh(HashMap<Mod, HashMap<Mod, ModCompatInfo>> repoMods) {
        DefaultListModel<Mod> repoModsModel = new DefaultListModel<>();
        for (Mod repoMod : repoMods.keySet()) {
            repoModsModel.add(0, repoMod);
        }
        setModel(repoModsModel);

    }
}