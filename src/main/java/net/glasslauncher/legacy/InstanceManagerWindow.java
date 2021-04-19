package net.glasslauncher.legacy;

import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.FileUtils;
import net.glasslauncher.legacy.components.*;
import net.glasslauncher.legacy.util.InstanceManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

class InstanceManagerWindow extends JDialog {
    private JPanel panel;
    private JPanel deletePanel = new JPanel();

    private JCheckBox hideMSCheckbox;
    private JCheckBox disableThemeCheckbox;
    private JCheckBox ignoreInstanceVersionCheckbox;

    InstanceManagerWindow(Frame frame) {
        super(frame);
        setModal(true);
        setLayout(new GridLayout());
        setResizable(false);
        panel = new JPanelBackgroundImage(Main.class.getResource("assets/blogbackground.png"));
        panel.setLayout(new GridLayout());
        add(panel);
        setTitle("Instance Manager");
        addWindowListener(new WindowAdapter() {
                              public void windowClosing(WindowEvent we) {
                                      Main.LOGGER.info("Closing instance manager...");
                                      dispose();
                              }
                          }
        );
        setBounds(0, 0, 580, 340);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setBounds(0, 0, 580, 340);
        tabbedPane.setBorder(new EmptyBorder(0, -2, -2, -2));
        tabbedPane.addTab("Create", makeCreateTab());
        tabbedPane.addTab("Delete Instance", makeDeleteTab());
        tabbedPane.addTab("Launcher Settings", makeLauncherTab());

        panel.add(tabbedPane);

        setLocationRelativeTo(frame);
        setVisible(true);
    }

    private JPanel makeCreateTab() {
        JPanel createPanel = new JPanel();
        createPanel.setOpaque(false);
        createPanel.setBounds(0, 0, 580, 340);
        createPanel.setLayout(null);

        // Local Modpack zip install.
        HintTextField installModpackDir = new JTextFieldFancy("Paste the path to your Modpack zip here!");
        installModpackDir.setBounds(155, 5, 355, 22);

        JButton installModpackDirButton = new JButtonScalingFancy();
        installModpackDirButton.setText("Install Local Modpack");
        installModpackDirButton.setBounds(5, 5, 150, 22);
        installModpackDirButton.addActionListener(event -> {
            ProgressWindow progressWindow = new ProgressWindow(this, "Installing Local Modpack...");
            Thread thread = new Thread(() -> {
                InstanceManager.installModpack(installModpackDir.getText(), progressWindow);
                progressWindow.dispose();
            });
            progressWindow.setThread(thread);
            thread.start();
            if (progressWindow.isDisplayable()) {
                progressWindow.setVisible(true);
            }
        });

        JButton instanceZipSelectButton = new JButtonScalingFancy();
        instanceZipSelectButton.setText("...");
        instanceZipSelectButton.addActionListener(event -> {
            FileDialog fileChooser = new FileDialog(this, "Select Modpack");
            fileChooser.setFilenameFilter((e, str) -> str.endsWith(".zip"));
            fileChooser.setVisible(true);
            try {
                String file = fileChooser.getFiles()[0].getAbsolutePath();
                installModpackDir.setText(file);
            } catch (NullPointerException | ArrayIndexOutOfBoundsException ignored) {}
        });
        instanceZipSelectButton.setBounds(515, 5, 30, 22);

        // Remote Modpack zip install.
        HintTextField installModpackURL = new JTextFieldFancy("Paste the URL to your Modpack zip here!");
        installModpackURL.setBounds(155, 34, 390, 22);

        JButton installModpackURLButton = new JButtonScalingFancy();
        installModpackURLButton.setText("Install Modpack from URL");
        installModpackURLButton.setBounds(5, 34, 150, 22);
        installModpackURLButton.addActionListener(event -> {
            ProgressWindow progressWindow = new ProgressWindow(this, "Installing Remote Modpack...");
            Thread thread = new Thread(() -> {
                InstanceManager.installModpack(installModpackURL.getText(), progressWindow);
                progressWindow.dispose();
            });
            progressWindow.setThread(thread);
            thread.start();
            if (progressWindow.isDisplayable()) {
                progressWindow.setVisible(true);
            }
        });

        // Make blank instance
        HintTextField instanceName = new JTextFieldFancy("Instance name");
        instanceName.setBounds(155, 63, 194, 22);

        JComboBox<String> instanceVersion = new JComboBox<>();
        for (String version : Config.getMcVersions().getClient().keySet()) {
            instanceVersion.addItem(version);
        }
        instanceVersion.setBounds(351, 63, 194, 22);

        JButton instanceVersionButton = new JButtonScalingFancy();
        instanceVersionButton.setText("Create Blank Instance");
        instanceVersionButton.addActionListener((e) -> {
            ProgressWindow progressWindow = new ProgressWindow(this, "Creating New Instance...");
            Thread thread = new Thread(() -> {
                InstanceManager.createBlankInstance((String) instanceVersion.getSelectedItem(), instanceName.getText(), progressWindow);
                progressWindow.dispose();
            });
            progressWindow.setThread(thread);
            thread.start();
            if (progressWindow.isDisplayable()) {
                progressWindow.setVisible(true);
            }
        });
        instanceVersionButton.setBounds(5, 63, 150, 22);

        JButton instanceFolderButton = new JButtonScalingFancy();
        instanceFolderButton.setText("Open Instances Folder");
        instanceFolderButton.addActionListener((e) -> {
            Main.LOGGER.info("Opening instances folder...");
            try {
                Desktop.getDesktop().open(new File(CommonConfig.getGlassPath() + "instances"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        instanceFolderButton.setBounds(5, 247, 150, 22);

        createPanel.add(installModpackDir);
        createPanel.add(installModpackDirButton);
        createPanel.add(instanceZipSelectButton);
        createPanel.add(installModpackURL);
        createPanel.add(installModpackURLButton);
        createPanel.add(instanceName);
        createPanel.add(instanceVersion);
        createPanel.add(instanceVersionButton);
        createPanel.add(instanceFolderButton);

        return createPanel;
    }

    private JScrollPane makeDeleteTab() {
        deletePanel.setLayout(new BoxLayout(deletePanel, BoxLayout.Y_AXIS));
        deletePanel.setOpaque(false);
        JScrollPane deletePanelPane = new JScrollPane(deletePanel);
        deletePanelPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        deletePanelPane.setOpaque(false);
        deletePanelPane.getViewport().setOpaque(false);

        updateInstanceList();

        return deletePanelPane;
    }

    private void updateInstanceList() {
        (new File(CommonConfig.getGlassPath() + "instances")).mkdirs();
        deletePanel.removeAll();
        deletePanel.repaint();
        for (File instance : (Objects.requireNonNull(new File(CommonConfig.getGlassPath() + "instances").listFiles()))) {
            if (instance.isDirectory()) {
                JButton deleteButton = new JButtonScalingFancy();
                deleteButton.setText("Delete \"" + instance.getName() + "\".");
                deleteButton.setMinimumSize(new Dimension(540, 22));
                deleteButton.setMaximumSize(new Dimension(540, 22));
                deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                deleteButton.addActionListener((e) -> {
                    try {
                        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete \"" + instance.getName() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
                        if (response == JOptionPane.YES_OPTION) {
                            FileUtils.delete(instance);
                            updateInstanceList();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                deletePanel.add(deleteButton);
            }
        }
    }

    private JPanel makeLauncherTab() {
        JPanel launcherPanel = new JPanel();
        launcherPanel.setOpaque(false);
        launcherPanel.setBounds(0, 0, 580, 340);
        launcherPanel.setLayout(null);

        JLabelFancy hideMSButtonLabel = new JLabelFancy("Hide MS Login Button:");
        hideMSButtonLabel.setBounds(5, 5, 120, 20);

        hideMSCheckbox = new JCheckBox();
        hideMSCheckbox.setOpaque(false);
        hideMSCheckbox.setBounds(135, 6, 20, 20);
        hideMSCheckbox.setSelected(Config.getLauncherConfig().isHidingMSButton());

        JLabelFancy disableThemeLabel = new JLabelFancy("Disable Custom Theme:");
        disableThemeLabel.setBounds(5, 29, 120, 20);

        disableThemeCheckbox = new JCheckBox();
        disableThemeCheckbox.setOpaque(false);
        disableThemeCheckbox.setBounds(135, 30, 20, 20);
        disableThemeCheckbox.setSelected(Config.getLauncherConfig().isThemeDisabled());

        JLabelFancy ignoreInstanceVersionLabel = new JLabelFancy("Ignore Instance Version:");
        ignoreInstanceVersionLabel.setBounds(5, 53, 140, 20);

        ignoreInstanceVersionCheckbox = new JCheckBox();
        ignoreInstanceVersionCheckbox.setOpaque(false);
        ignoreInstanceVersionCheckbox.setBounds(155, 54, 20, 20);
        ignoreInstanceVersionCheckbox.setSelected(Config.getLauncherConfig().isIgnoreInstanceVersion());

        JButtonScalingFancy clearVersionCache = new JButtonScalingFancy();
        clearVersionCache.setText("Clear Version Cache");
        clearVersionCache.addActionListener((e -> {
            int response = JOptionPane.showConfirmDialog(this, "This will clear " + (Math.round((size(Paths.get(CommonConfig.getGlassPath(), "cache/versions"))/1048576.0) * 100) / 100.0) + "MB from \"cache/versions\"");
            if (response == JOptionPane.YES_OPTION) {
                FileUtils.delete(new File(CommonConfig.getGlassPath(), "cache/versions"));
            }
        }));
        clearVersionCache.setBounds(5, 175, 140, 20);

        JButtonScalingFancy clearIntermediaryCache = new JButtonScalingFancy();
        clearIntermediaryCache.setText("Clear Intermediary Cache");
        clearIntermediaryCache.addActionListener((e -> {
            int response = JOptionPane.showConfirmDialog(this, "This will clear " + (Math.round((size(Paths.get(CommonConfig.getGlassPath(), "cache/intermediary_mappings"))/1048576.0) * 100) / 100.0) + "MB from \"cache/intermediary_mappings\"");
            if (response == JOptionPane.YES_OPTION) {
                FileUtils.delete(new File(CommonConfig.getGlassPath(), "cache/intermediary_mappings"));
            }
        }));
        clearIntermediaryCache.setBounds(5, 199, 140, 20);

        JButtonScalingFancy clearSkinCache = new JButtonScalingFancy();
        clearSkinCache.setText("Clear Skin Cache");
        clearSkinCache.addActionListener((e -> {
            int response = JOptionPane.showConfirmDialog(this, "This will clear " + (Math.round((size(Paths.get(CommonConfig.getGlassPath(), "cache/webproxy"))/1048576.0) * 100) / 100.0) + "MB from \"cache/webproxy\"");
            if (response == JOptionPane.YES_OPTION) {
                FileUtils.delete(new File(CommonConfig.getGlassPath(), "cache/webproxy"));
            }
        }));
        clearSkinCache.setBounds(5, 223, 140, 20);

        JButtonScalingFancy clearResourceCache = new JButtonScalingFancy();
        clearResourceCache.setText("Clear Resource Cache");
        clearResourceCache.addActionListener((e -> {
            int response = JOptionPane.showConfirmDialog(this, "This will clear " + (Math.round((size(Paths.get(CommonConfig.getGlassPath(), "cache/resources"))/1048576.0) * 100) / 100.0) + "MB from \"cache/resources\"");
            if (response == JOptionPane.YES_OPTION) {
                FileUtils.delete(new File(CommonConfig.getGlassPath(), "cache/resources"));
            }
        }));
        clearResourceCache.setBounds(5, 247, 140, 20);


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                Config.getLauncherConfig().setHidingMSButton(hideMSCheckbox.isSelected());
                Config.getLauncherConfig().setThemeDisabled(disableThemeCheckbox.isSelected());
                Config.getLauncherConfig().setIgnoreInstanceVersion(disableThemeCheckbox.isSelected());
                Config.getLauncherConfig().saveFile();
            }
        });

        launcherPanel.add(hideMSButtonLabel);
        launcherPanel.add(hideMSCheckbox);
        launcherPanel.add(disableThemeLabel);
        launcherPanel.add(disableThemeCheckbox);
        launcherPanel.add(ignoreInstanceVersionLabel);
        launcherPanel.add(ignoreInstanceVersionCheckbox);
        launcherPanel.add(clearVersionCache);
        launcherPanel.add(clearIntermediaryCache);
        launcherPanel.add(clearSkinCache);
        launcherPanel.add(clearResourceCache);

        return launcherPanel;
    }

    /**
     * Attempts to calculate the size of a file or directory.
     *
     * <p>
     * Since the operation is non-atomic, the returned value may be inaccurate.
     * However, this method is quick and does its best.
     * https://stackoverflow.com/a/19877372
     */
    public static long size(Path path) {

        final AtomicLong size = new AtomicLong(0);

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {

                    System.out.println("skipped: " + file + " (" + exc + ")");
                    // Skip folders that can't be traversed
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

                    if (exc != null)
                        System.out.println("had trouble traversing: " + dir + " (" + exc + ")");
                    // Ignore errors traversing a folder
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
        }

        return size.get();
    }
}
