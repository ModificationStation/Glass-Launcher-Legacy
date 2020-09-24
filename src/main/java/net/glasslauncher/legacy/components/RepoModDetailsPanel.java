package net.glasslauncher.legacy.components;

import net.glasslauncher.common.FileUtils;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.OptionsWindow;
import net.glasslauncher.legacy.components.events.OnModChange;
import net.glasslauncher.legacy.util.LinkRedirector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;

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
            if (repoMod != null) {
                URL url;
                try {
                    url = repoMod.getLatestVersion().getDownloadURL("client", "jar");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return;
                }
                FileUtils.downloadFile(String.valueOf(url), Config.getInstancePath(instance) + ".minecraft/mods", null, repoMod.getId() + "-" + repoMod.getLatestVersion().getVersion() + ".jar");
            }
        });

        JButton openPageButton = new JButtonScalingFancy();
        openPageButton.setText("Open Page");
        openPageButton.addActionListener((actionEvent) -> {
            if (repoMod != null) {
                LinkRedirector.openLinkInSystemBrowser(net.glasslauncher.repo.api.Config.REPOSITORY_URL + "mod/" + repoMod.getId());
            }
        });

        buttons.add(downloadButton);
        buttons.add(Box.createRigidArea(new Dimension(40, 0)));
        buttons.add(openPageButton);
    }
}
