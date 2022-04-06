package net.glasslauncher.legacy.components;

import net.glasslauncher.common.FileUtils;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.ProgressWindow;
import net.glasslauncher.legacy.components.templates.DetailsPanel;
import net.glasslauncher.legacy.util.LinkRedirector;
import net.glasslauncher.repo.api.RepoConfig;
import net.glasslauncher.repo.api.mod.jsonobj.Mod;
import net.glasslauncher.repo.api.mod.jsonobj.Version;
import org.commonmark.node.Node;

import javax.swing.*;
import java.awt.*;
import java.net.*;

public class RepoModDescriptionPanel extends DetailsPanel {

    private Version version = null;
    private final String instance;
    private final Frame parent;

    private JButton downloadButton;

    public RepoModDescriptionPanel(Frame parent, String instance, Mod mod) {
        super();
        this.instance = instance;
        this.parent = parent;

        name.setText("<style>" +
                Config.getCSS() + "</style><body><div style=\"font-size: 18px;\">" +
                mod.getName() + " <sup style=\"font-size: 10px;\">by " +
                mod.getAuthors()[0].getUsername() + "</sup></div></body>");
        Node document = PARSER.parse(mod.getDescription().replace("\n", "  \n"));
        description.setText(RENDERER.render(document));
    }

    @Override
    public void setMod(Object version) {
        this.version = (Version) version;

        name.setText("<style>" +
                Config.getCSS() + "</style><body><div style=\"font-size: 18px;\">" +
                this.version.getVersion() + " <sup style=\"font-size: 10px;\"></div></body>");
        Node document = PARSER.parse(this.version.getDescription().replace("\n", "  \n"));
        description.setText(RENDERER.render(document));
        downloadButton.setEnabled(true);
    }

    @Override
    public void setupButtons(JPanel buttons) {

        downloadButton = new JButtonScalingFancy();
        downloadButton.setText("  Download  ");
        downloadButton.addActionListener((actionEvent) -> {
            if (version != null && version.isHasClient()) {
                try {
                    URL url = version.getDownloadURL(Version.CLIENT, "jar");
                    String path;
                    if (version.getType().equals("Mod Folder")) {
                        Main.LOGGER.info("Downloading mod to mods folder.");
                        path = Config.getInstancePath(instance) + ".minecraft/mods";
                    }
                    else if (version.getType().equals("Base Edit")) {
                        Main.LOGGER.info("Downloading mod to jar mods folder.");
                        path = Config.getInstancePath(instance) + "mods";
                    }
                    else {
                        JOptionPane.showMessageDialog(this, "This mod has a client file, but must be installed manually because of unknown type!\nOpening URL in browser.", "Info", JOptionPane.INFORMATION_MESSAGE);
                        LinkRedirector.openLinkInSystemBrowser(RepoConfig.REPOSITORY_URL + "mod/" + version.getParentMod() + "/versions/" + version.getVersion());
                        return;
                    }
                    ProgressWindow progressWindow = new ProgressWindow(parent, "Downloading Mod");
                    progressWindow.setProgressMax(2);
                    new Thread(() -> {
                        progressWindow.setVisible(true);
                    }).start();
                    progressWindow.setProgress(1);
                    progressWindow.setProgressText("Downloading " + version.getParentMod() + " " + version.getVersion());
                    boolean downloaded = FileUtils.downloadFile(String.valueOf(url), path, null, version.getParentMod() + "-" + version.getVersion() + ".jar");
                    progressWindow.setProgress(2);
                    progressWindow.setProgressText("Done!");
                    progressWindow.dispose();
                    if (downloaded) {
                        JOptionPane.showMessageDialog(parent, "Successfully downloaded " + version.getParentMod() + " " + version.getVersion());
                    }
                    else {
                        JOptionPane.showMessageDialog(parent, "Unable to download " + version.getParentMod() + " " + version.getVersion(), "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "This mod does not have a client file!", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
        downloadButton.setEnabled(false);

        JButton openPageButton = new JButtonScalingFancy();
        openPageButton.setText("  Open Page  ");
        openPageButton.addActionListener((actionEvent) -> {
            if (version != null) {
                LinkRedirector.openLinkInSystemBrowser(RepoConfig.REPOSITORY_URL + "mod/" + version.getParentMod());
            }
        });

        buttons.add(downloadButton);
        buttons.add(Box.createRigidArea(new Dimension(40, 0)));
        buttons.add(openPageButton);
    }
}
