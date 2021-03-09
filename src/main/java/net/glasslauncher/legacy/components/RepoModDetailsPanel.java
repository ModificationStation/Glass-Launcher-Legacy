package net.glasslauncher.legacy.components;

import net.glasslauncher.common.FileUtils;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.components.events.OnModChange;
import net.glasslauncher.legacy.util.LinkRedirector;
import net.glasslauncher.repo.api.RepoConfig;
import net.glasslauncher.repo.api.mod.jsonobj.Version;

import javax.swing.*;
import java.awt.*;
import java.net.*;

public class RepoModDetailsPanel extends ModDetailsPanel {
    public RepoModDetailsPanel(String instance) {
        super(instance);
    }

    @Override
    void onModChange() {
        for (Component component : super.componentArrayList) {
            if (component instanceof OnModChange) {
                ((OnModChange) component).onRepoModChange(repoMod);
            }
        }
    }

    @Override
    void setupButtons(JPanel buttons) {

        JButton downloadButton = new JButtonScalingFancy();
        downloadButton.setText("Download");
        downloadButton.addActionListener((actionEvent) -> {
            if (repoMod != null && repoMod.getLatestVersion() != null && repoMod.getLatestVersion().isHasClient()) {
                try {
                    Version version = repoMod.getLatestVersion();
                    URL url = version.getDownloadURL("client", "jar");
                    String path;
                    if (version.getType().equals("Mod Folder")) {
                        Main.LOGGER.info("Downloading mod to mods folder.");
                        path = Config.getInstancePath(instance) + ".minecraft/mods";
                    }
                    else if (version.getType().equals("Base Edit") || version.getType().equals("jarmod")) {
                        Main.LOGGER.info("Downloading mod to jar mods folder.");
                        path = Config.getInstancePath(instance) + "mods";
                    }
                    else {
                        JOptionPane.showMessageDialog(this, "This mod has a client file, but must be installed manually!\nOpening URL in browser.", "Info", JOptionPane.INFORMATION_MESSAGE);
                        LinkRedirector.openLinkInSystemBrowser(RepoConfig.REPOSITORY_URL + "mod/" + repoMod.getId() + "/versions/" + version.getVersion());
                        return;
                    }
                    FileUtils.downloadFile(String.valueOf(url), path, null, repoMod.getId() + "-" + repoMod.getLatestVersion().getVersion() + ".jar");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "This mod does not have a client file!", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton openPageButton = new JButtonScalingFancy();
        openPageButton.setText("Open Page");
        openPageButton.addActionListener((actionEvent) -> {
            if (repoMod != null) {
                LinkRedirector.openLinkInSystemBrowser(RepoConfig.REPOSITORY_URL + "mod/" + repoMod.getId());
            }
        });

        buttons.add(downloadButton);
        buttons.add(Box.createRigidArea(new Dimension(40, 0)));
        buttons.add(openPageButton);
    }
}
