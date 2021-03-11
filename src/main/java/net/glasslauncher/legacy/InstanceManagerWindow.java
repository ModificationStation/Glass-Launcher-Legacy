package net.glasslauncher.legacy;

import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.FileUtils;
import net.glasslauncher.legacy.components.HintTextField;
import net.glasslauncher.legacy.components.JButtonScalingFancy;
import net.glasslauncher.legacy.components.JLabelFancy;
import net.glasslauncher.legacy.components.JPanelBackgroundImage;
import net.glasslauncher.legacy.components.JTextFieldFancy;
import net.glasslauncher.legacy.util.InstanceManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class InstanceManagerWindow extends JDialog {
    private JPanel deletePanel = new JPanel();
    private JPanel panel;

    private JCheckBox hideMSCheckbox;
    private JCheckBox disableThemeCheckbox;

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

        panel.add(tabbedPane);

        setLocationRelativeTo(panel);
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
                Desktop.getDesktop().open(new File(CommonConfig.GLASS_PATH + "instances"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        instanceFolderButton.setBounds(5, 247, 150, 22);

        JLabelFancy hideMSButtonLabel = new JLabelFancy("Hide MS Login Button:");
        hideMSButtonLabel.setBounds(400, 247, 120, 20);

        hideMSCheckbox = new JCheckBox();
        hideMSCheckbox.setOpaque(false);
        hideMSCheckbox.setBounds(530, 247, 20, 20);
        hideMSCheckbox.setSelected(Config.getLauncherConfig().isHidingMSButton());

        JLabelFancy disableThemeLabel = new JLabelFancy("Disable Custom Theme:");
        disableThemeLabel.setBounds(400, 223, 120, 20);

        disableThemeCheckbox = new JCheckBox();
        disableThemeCheckbox.setOpaque(false);
        disableThemeCheckbox.setBounds(530, 223, 20, 20);
        disableThemeCheckbox.setSelected(Config.getLauncherConfig().isThemeDisabled());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                Config.getLauncherConfig().setHidingMSButton(hideMSCheckbox.isSelected());
                Config.getLauncherConfig().setThemeDisabled(disableThemeCheckbox.isSelected());
                Config.getLauncherConfig().saveFile();
            }
        });

        createPanel.add(installModpackDir);
        createPanel.add(installModpackDirButton);
        createPanel.add(instanceZipSelectButton);
        createPanel.add(installModpackURL);
        createPanel.add(installModpackURLButton);
        createPanel.add(instanceName);
        createPanel.add(instanceVersion);
        createPanel.add(instanceVersionButton);
        createPanel.add(instanceFolderButton);
        createPanel.add(hideMSButtonLabel);
        createPanel.add(hideMSCheckbox);
        createPanel.add(disableThemeLabel);
        createPanel.add(disableThemeCheckbox);

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
        (new File(CommonConfig.GLASS_PATH + "instances")).mkdirs();
        deletePanel.removeAll();
        deletePanel.repaint();
        for (File instance : (Objects.requireNonNull(new File(CommonConfig.GLASS_PATH + "instances").listFiles()))) {
            if (instance.isDirectory()) {
                JButton deleteButton = new JButtonScalingFancy();
                deleteButton.setText("Delete \"" + instance.getName() + "\".");
                deleteButton.setMinimumSize(new Dimension(540, 22));
                deleteButton.setMaximumSize(new Dimension(540, 22));
                deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                deleteButton.addActionListener((e) -> {
                    try {
                        FileUtils.delete(instance);
                        updateInstanceList();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                deletePanel.add(deleteButton);
            }
        }
    }
}
