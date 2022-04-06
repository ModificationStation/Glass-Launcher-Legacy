package net.glasslauncher.legacy;

import net.glasslauncher.legacy.components.CompatList;
import net.glasslauncher.legacy.components.CompatibilityDetailsPanel;
import net.glasslauncher.legacy.components.JPanelBackgroundImage;
import net.glasslauncher.legacy.jsontemplate.Mod;
import net.glasslauncher.legacy.jsontemplate.ModCompatInfo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;

public class ModCompatWindow extends JDialog {

    private final HashMap<Mod, HashMap<Mod, ModCompatInfo>> compatInfo;

    public ModCompatWindow(Window frame, HashMap<Mod, HashMap<Mod, ModCompatInfo>> compatInfo) {
        this.compatInfo = compatInfo;
        setModal(true);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle("Mod Compatibility");

        JPanel panel = new JPanelBackgroundImage(Main.class.getResource("assets/blogbackground.png"));
        add(panel);

        JTabbedPane tabpane = new JTabbedPane();
        tabpane.setOpaque(false);
        tabpane.setPreferredSize(new Dimension(837, 448 - tabpane.getInsets().top - tabpane.getInsets().bottom));
        tabpane.setBorder(new EmptyBorder(0, -2, -2, -2));

        tabpane.addTab("Compatibility", makeCompatibilityTab());
        panel.add(tabpane);
        pack();
        setLocationRelativeTo(frame);
    }

    private JPanel makeCompatibilityTab() {
        JPanel modsPanel = new JPanel();
        modsPanel.setOpaque(false);
        modsPanel.setLayout(null);

        CompatibilityDetailsPanel modDetailsPanel = new CompatibilityDetailsPanel(compatInfo);

        JScrollPane modListScroll = new JScrollPane();
        CompatList loaderModDragDropList = new CompatList(modDetailsPanel);
        if (!Config.getLauncherConfig().isThemeDisabled()) {
            modListScroll.getViewport().setBackground(new Color(52, 52, 52));
            loaderModDragDropList.setBackground(new Color(52, 52, 52));
        }
        loaderModDragDropList.refresh(compatInfo);

        modListScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        modListScroll.setBounds(20, 20, 200, 200);
        modListScroll.setViewportView(loaderModDragDropList);

        modsPanel.add(modListScroll);
        modsPanel.add(modDetailsPanel);

        return modsPanel;
    }
}
