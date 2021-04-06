package net.glasslauncher.legacy;

import net.glasslauncher.legacy.components.JPanelBackgroundImage;
import net.glasslauncher.legacy.components.RepoModDescriptionPanel;
import net.glasslauncher.legacy.components.RepoModVersionList;
import net.glasslauncher.legacy.components.templates.DetailsPanel;
import net.glasslauncher.repo.api.mod.RepoReader;
import net.glasslauncher.repo.api.mod.jsonobj.Mod;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

public class RepoModDetailsDialog extends JDialog {

    private final String instName;
    private final Frame parent;
    private Mod mod = null;

    public RepoModDetailsDialog(Frame parent, String modid, String instance) {
        super(parent, true);
        instName = instance;
        this.parent = parent;
        try {
            mod = RepoReader.getMod(modid);
        } catch (IOException e) {
            Main.LOGGER.severe("Failed to get mod info for \"" + modid + "\"");
            e.printStackTrace();
            return;
        }
        setPreferredSize(new Dimension(837, 448));
        setMinimumSize(new Dimension(837, 448));

        setupGUI();
        setLocationRelativeTo(parent);
    }

    @Override
    public void setVisible(boolean b) {
        if (mod != null) {
            super.setVisible(b);
        }
    }

    private void setupGUI() {
        JPanel panel = new JPanelBackgroundImage(Main.class.getResource("assets/blogbackground.png"));
        panel.setLayout(null);
        DetailsPanel detailsPanel = new RepoModDescriptionPanel(parent, instName, mod);
        panel.add(detailsPanel);
        RepoModVersionList versionList = new RepoModVersionList(detailsPanel, mod.getId());
        versionList.refresh(Arrays.asList(mod.getVersions()));

        JScrollPane versionListScroll = new JScrollPane(versionList);
        if (!Config.getLauncherConfig().isThemeDisabled()) {
            versionListScroll.getViewport().setBackground(new Color(52, 52, 52));
            versionList.setBackground(new Color(52, 52, 52));
        }
        versionListScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        versionListScroll.setBounds(20, 20, 200, 380);

        panel.add(versionListScroll);
        add(panel);
    }
}
