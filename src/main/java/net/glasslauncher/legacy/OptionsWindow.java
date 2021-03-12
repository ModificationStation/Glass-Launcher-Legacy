package net.glasslauncher.legacy;

import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.JsonConfig;
import net.glasslauncher.legacy.components.DragDropList;
import net.glasslauncher.legacy.components.JButtonScalingFancy;
import net.glasslauncher.legacy.components.JDetailsTable;
import net.glasslauncher.legacy.components.JLabelFancy;
import net.glasslauncher.legacy.components.JPanelBackgroundImage;
import net.glasslauncher.legacy.components.JTextFieldFancy;
import net.glasslauncher.legacy.components.LocalModDetailsPanel;
import net.glasslauncher.legacy.components.handlers.RepoModTableModel;
import net.glasslauncher.legacy.components.templates.DetailsPanel;
import net.glasslauncher.legacy.components.ModLocalList;
import net.glasslauncher.legacy.components.RepoModVersionList;
import net.glasslauncher.legacy.jsontemplate.InstanceConfig;
import net.glasslauncher.legacy.jsontemplate.Mod;
import net.glasslauncher.legacy.jsontemplate.ModList;
import net.glasslauncher.legacy.mc.LocalMods;
import net.glasslauncher.legacy.util.InstanceManager;
import net.glasslauncher.repo.api.mod.RepoReader;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class OptionsWindow extends JDialog {
    private Frame parent;
    private JPanel panel;
    private InstanceConfig instanceConfig;
    private ModList modList;
    private ArrayList<Mod> jarMods;
    private DragDropList modDragDropList;
    private RepoModVersionList modRepoList;

    private DragDropList loaderModDragDropList;
    private ArrayList<Mod> loaderMods;

    private String instPath;
    private String instName;

    private JTextFieldFancy javaargs;
    private JTextFieldFancy minram;
    private JTextFieldFancy maxram;
    private JCheckBox skinproxy;
    private JCheckBox capeproxy;
    private JCheckBox soundproxy;
    private JCheckBox loginproxy;
    private JComboBox<String> instanceVersion;

    /**
     * Sets up options window for given instance.
     * @param frame Frame object to block while open.
     * @param instance Target instance.
     */
    public OptionsWindow(Frame frame, String instance) {
        super(frame);
        parent = frame;
        setModal(true);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle("Instance Options");
        this.panel = new JPanelBackgroundImage(Main.class.getResource("assets/blogbackground.png"));
        add(this.panel);

        instName = instance;
        instPath = CommonConfig.GLASS_PATH + "instances/" + instance + "/";
        if (!(new File(instPath)).exists()) {
            JOptionPane.showMessageDialog(this, "Selected instance does not exist, or one hasn't been selected.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        instanceConfig = (InstanceConfig) JsonConfig.loadConfig(instPath + "instance_config.json", InstanceConfig.class);
        if (instanceConfig == null) {
            instanceConfig = new InstanceConfig(instPath + "instance_config.json");
        }
        modList = (ModList) JsonConfig.loadConfig(instPath + "mods/mods.json", ModList.class);
        if (modList == null) {
            modList = new ModList(instPath + "mods/mods.json");
        }

        JTabbedPane tabpane = new JTabbedPane();
        tabpane.setOpaque(false);
        tabpane.setPreferredSize(new Dimension(837, 448 - tabpane.getInsets().top - tabpane.getInsets().bottom));
        tabpane.setBorder(new EmptyBorder(0, -2, -2, -2));

        tabpane.addTab("Settings", makeInstSettings());
        tabpane.addTab("Jar Mods", makeJarMods());
        tabpane.addTab("Loader Mods", makeLoaderMods());
        tabpane.addTab("Mod Repo", makeModRepo());

        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    instanceConfig.setJavaArgs(javaargs.getText());
                    instanceConfig.setMaxRam(maxram.getText());
                    instanceConfig.setMinRam(minram.getText());
                    instanceConfig.setProxySkin(skinproxy.isSelected());
                    instanceConfig.setProxyCape(capeproxy.isSelected());
                    instanceConfig.setProxySound(soundproxy.isSelected());
                    instanceConfig.setProxyLogin(loginproxy.isSelected());
                    instanceConfig.setVersion((String) instanceVersion.getSelectedItem());

                    modList.setJarMods(new ArrayList<>());
                    ListModel listModel = modDragDropList.model;
                    for (int i = 0; i< listModel.getSize(); i++) {
                        try {
                            Mod mod = (Mod) listModel.getElementAt(i);
                            modList.getJarMods().add(mod);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        modList.saveFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        instanceConfig.saveFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                  }
            }
        );

        this.panel.add(tabpane);
        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    /**
     * Makes the things that do the thing for the instance JSON.
     *
     * @return The panel object containing all created components.
     */
    private JPanel makeInstSettings() {
        JPanel instsettings = new JPanel();
        instsettings.setOpaque(false);
        instsettings.setLayout(null);

        JLabelFancy javaargslabel = new JLabelFancy("Java Arguments:");
        javaargslabel.setBounds(20, 22, 100, 20);
        instsettings.add(javaargslabel);

        javaargs = new JTextFieldFancy("Java Arguments");
        javaargs.setBounds(150, 20, 400, 24);
        javaargs.setText(instanceConfig.getJavaArgs());
        instsettings.add(javaargs);

        JLabelFancy ramalloclabel = new JLabelFancy("RAM Allocation:");
        ramalloclabel.setBounds(20, 50, 100, 20);
        instsettings.add(ramalloclabel);

        JLabelFancy maxramlabel = new JLabelFancy("Maximum:");
        maxramlabel.setBounds(150, 50, 65, 20);
        instsettings.add(maxramlabel);

        maxram = new JTextFieldFancy("Maximum RAM");
        maxram.setBounds(245, 48, 100, 24);
        maxram.setText(instanceConfig.getMaxRam());
        instsettings.add(maxram);

        JLabelFancy minramlabel = new JLabelFancy("Minimum:");
        minramlabel.setBounds(360, 50, 65, 20);
        instsettings.add(minramlabel);

        minram = new JTextFieldFancy("Minimum RAM");
        minram.setBounds(450, 48, 100, 24);
        minram.setText(instanceConfig.getMinRam());
        instsettings.add(minram);

        JLabelFancy skinproxylabel = new JLabelFancy("Enable Skin Proxy:");
        skinproxylabel.setBounds(20, 96, 120, 20);
        instsettings.add(skinproxylabel);

        skinproxy = new JCheckBox();
        skinproxy.setOpaque(false);
        skinproxy.setBounds(150, 97, 20, 20);
        skinproxy.setSelected(instanceConfig.isProxySkin());
        instsettings.add(skinproxy);

        JLabelFancy capeproxylabel = new JLabelFancy("Enable Cape Proxy:");
        capeproxylabel.setBounds(20, 124, 120, 20);
        instsettings.add(capeproxylabel);

        capeproxy = new JCheckBox();
        capeproxy.setOpaque(false);
        capeproxy.setBounds(150, 125, 20, 20);
        capeproxy.setSelected(instanceConfig.isProxyCape());
        instsettings.add(capeproxy);

        JLabelFancy soundproxylabel = new JLabelFancy("Enable Sound Proxy:");
        soundproxylabel.setBounds(20, 152, 120, 20);
        instsettings.add(soundproxylabel);

        soundproxy = new JCheckBox();
        soundproxy.setOpaque(false);
        soundproxy.setBounds(150, 153, 20, 20);
        soundproxy.setSelected(instanceConfig.isProxySound());
        instsettings.add(soundproxy);

        JLabelFancy loginproxylabel = new JLabelFancy("Enable Login Proxy:");
        loginproxylabel.setBounds(20, 180, 120, 20);
        instsettings.add(loginproxylabel);

        loginproxy = new JCheckBox();
        loginproxy.setOpaque(false);
        loginproxy.setBounds(150, 181, 20, 20);
        loginproxy.setSelected(instanceConfig.isProxyLogin());
        instsettings.add(loginproxy);

        JLabelFancy instanceVersionLabel = new JLabelFancy("Minecraft Version:");
        instanceVersionLabel.setBounds(20, 372, 120, 20);
        instsettings.add(instanceVersionLabel);

        instanceVersion = new JComboBox<>();
        for (String version : Config.getMcVersions().getClient().keySet()) {
            instanceVersion.addItem(version);
        }
        instanceVersion.addItem("none");
        instanceVersion.setSelectedItem(instanceConfig.getVersion());
        instanceVersion.setBounds(150, 370, 194, 22);
        instsettings.add(instanceVersion);

        return instsettings;
    }

    private JPanel makeJarMods() {
        JPanel modsPanel = new JPanel();
        modsPanel.setOpaque(false);
        modsPanel.setLayout(null);

        jarMods = new ArrayList<>();
        JScrollPane modListScroll = new JScrollPane();
        modDragDropList = new DragDropList(jarMods, instPath);
        if (!Config.getLauncherConfig().isThemeDisabled()) {
            modListScroll.getViewport().setBackground(new Color(52, 52, 52));
            modDragDropList.setBackground(new Color(52, 52, 52));
        }
        refreshJarModList();
        modListScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        modListScroll.setBounds(20, 20, 200, 200);
        modListScroll.setViewportView(modDragDropList);

        JButtonScalingFancy toggleModsButton = new JButtonScalingFancy();
        toggleModsButton.setText("Toggle Selected Mods");
        toggleModsButton.addActionListener(event -> {
            for (Mod mod : modDragDropList.getSelectedValuesList()) {
                mod.setEnabled(!mod.isEnabled());
            }
            modDragDropList.repaint();
        });
        toggleModsButton.setBounds(20, 230, 200, 22);

        JButtonScalingFancy applyModsButton = new JButtonScalingFancy();
        applyModsButton.setText("Apply Current Mod Configuration");
        applyModsButton.addActionListener(event -> {
            ProgressWindow progressWindow = new ProgressWindow(this, "Applying Mods");
            Thread thread = new Thread(() -> {
                progressWindow.setProgressMax(2);
                progressWindow.setProgress(0);
                progressWindow.setProgressText("Setting up");
                File vanillaJar = new File(instPath + ".minecraft/bin/minecraft_vanilla.jar");
                File moddedJar = new File(instPath + ".minecraft/bin/minecraft.jar");
                try {
                    if (vanillaJar.exists()) {
                        moddedJar.delete();
                        Files.copy(vanillaJar.toPath(), moddedJar.toPath());
                    } else {
                        Files.copy(moddedJar.toPath(), vanillaJar.toPath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                progressWindow.setProgress(1);
                progressWindow.setProgressText("Applying Mods");
                InstanceManager.addMods(instName, modDragDropList.model);
                progressWindow.setProgress(2);
                progressWindow.setProgressText("Done");
                progressWindow.dispose();
                JOptionPane.showMessageDialog(this, "Mods Applied!", "Info", JOptionPane.INFORMATION_MESSAGE);
            });
            progressWindow.setThread(thread);
            thread.start();
            if (progressWindow.isDisplayable()) {
                progressWindow.setVisible(true);
            }
        });
        applyModsButton.setBounds(20, 262, 200, 22);

        JButtonScalingFancy addModsButton = new JButtonScalingFancy();
        addModsButton.setText("Add Mods");
        addModsButton.addActionListener(event -> {
            FileDialog fileChooser = new FileDialog(this, "Select Mod");
            fileChooser.setFilenameFilter((e, str) -> str.endsWith(".jar") || str.endsWith(".zip"));
            fileChooser.setMultipleMode(true);
            fileChooser.setVisible(true);
            File[] files = fileChooser.getFiles();
            try {
                for (File file : files) {
                    Files.copy(file.toPath(), new File(instPath + "mods/" + file.getName()).toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {}
            refreshJarModList();
        });
        addModsButton.setBounds(20, 294, 200, 22);

        JButtonScalingFancy removeModsButton = new JButtonScalingFancy();
        removeModsButton.setText("Remove Selected Mods");
        removeModsButton.setForeground(new Color(185, 0, 0));
        removeModsButton.addActionListener(event -> {
            for (Object modObj : modDragDropList.getSelectedValuesList()) {
                Mod mod = (Mod) modObj;
                (new File(instPath + "mods/" + mod.getFileName())).delete();
            }
            refreshJarModList();
        });
        removeModsButton.setBounds(20, 326, 200, 22);

        modsPanel.add(modListScroll);
        modsPanel.add(toggleModsButton);
        modsPanel.add(applyModsButton);
        modsPanel.add(addModsButton);
        modsPanel.add(removeModsButton);

        return modsPanel;
    }

    private JPanel makeLoaderMods() {
        JPanel modsPanel = new JPanel();
        modsPanel.setOpaque(false);
        modsPanel.setLayout(null);

        DetailsPanel modDetailsPanel = new LocalModDetailsPanel();

        loaderMods = new ArrayList<>();
        JScrollPane modListScroll = new JScrollPane();
        loaderModDragDropList = new ModLocalList(loaderMods, instPath, modDetailsPanel);
        if (!Config.getLauncherConfig().isThemeDisabled()) {
            modListScroll.getViewport().setBackground(new Color(52, 52, 52));
            loaderModDragDropList.setBackground(new Color(52, 52, 52));
        }
        refreshLoaderModList();
        modListScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        modListScroll.setBounds(20, 20, 200, 200);
        modListScroll.setViewportView(loaderModDragDropList);

        JButtonScalingFancy toggleModsButton = new JButtonScalingFancy();
        toggleModsButton.setText("Toggle Selected Mods");
        toggleModsButton.addActionListener(event -> {
            for (Mod mod : loaderModDragDropList.getSelectedValuesList()) {
                try {
                    File modFile = new File(instPath + ".minecraft/mods/" + mod.getFileName());
                    if (modFile.getAbsolutePath().endsWith(".disabled")) {
                        Files.move(modFile.toPath(), Paths.get(modFile.toString().replaceFirst("\\.disabled$", "")));
                    } else {
                        Files.move(modFile.toPath(), Paths.get(modFile.getAbsolutePath() + ".disabled"));
                    }
                } catch (IOException e) {
                    Main.LOGGER.severe("Failed to rename mod \"" + mod.getFileName() + "\"!");
                    e.printStackTrace();
                }
                refreshLoaderModList();
            }
            loaderModDragDropList.repaint();
        });
        toggleModsButton.setBounds(20, 230, 200, 22);

        JButtonScalingFancy addModsButton = new JButtonScalingFancy();
        addModsButton.setText("Add Mods");
        addModsButton.addActionListener(event -> {
            FileDialog fileChooser = new FileDialog(this, "Select Mod");
            fileChooser.setFilenameFilter((e, str) -> str.endsWith(".jar") || str.endsWith(".zip"));
            fileChooser.setMultipleMode(true);
            fileChooser.setVisible(true);
            File[] files = fileChooser.getFiles();
            try {
                for (File file : files) {
                    Files.copy(file.toPath(), new File(instPath + ".minecraft/mods/" + file.getName()).toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {}
            refreshLoaderModList();
        });
        addModsButton.setBounds(20, 294, 200, 22);

        JButtonScalingFancy removeModsButton = new JButtonScalingFancy();
        removeModsButton.setText("Remove Selected Mods");
        removeModsButton.setForeground(new Color(185, 0, 0));
        removeModsButton.addActionListener(event -> {
            for (Object modObj : modDragDropList.getSelectedValuesList()) {
                Mod mod = (Mod) modObj;
                (new File(instPath + "mods/" + mod.getFileName())).delete();
            }
            refreshLoaderModList();
        });
        removeModsButton.setBounds(20, 326, 200, 22);

        modsPanel.add(modListScroll);
        modsPanel.add(toggleModsButton);
        modsPanel.add(addModsButton);
        modsPanel.add(removeModsButton);
        modsPanel.add(modDetailsPanel);

        return modsPanel;
    }

    JDetailsTable table;
    RepoModTableModel tableModel;

    private JPanel makeModRepo() {
        JPanel modRepoPanel = new JPanel();
        modRepoPanel.setLayout(new BorderLayout());
        modRepoPanel.setOpaque(false);
//        DetailsPanel modDetailsPanel = new RepoModDetailsPanel(instName);
//        modRepoList = new ModRepoList(modDetailsPanel);
//        modRepoPanel.add(modDetailsPanel);
        tableModel = new RepoModTableModel();
        table = new JDetailsTable(parent, tableModel, instName);
        table.setFillsViewportHeight(true);

        // Async cause otherwise options freezes when opening for 1-5 seconds.
        new Thread(this::refreshRepo).start();

        JScrollPane modListScroll = new JScrollPane();
        if (!Config.getLauncherConfig().isThemeDisabled()) {
            modListScroll.getViewport().setBackground(new Color(52, 52, 52));
        }
        modListScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        modListScroll.setViewportView(table);
        modRepoPanel.add(modListScroll, BorderLayout.CENTER);

        return modRepoPanel;
    }

    private void refreshRepo() {
        try {
            tableModel.setMods(Arrays.asList(RepoReader.getMods()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshJarModList() {
        jarMods = new ArrayList<>();
        File modsFolder = new File(instPath + "mods");
        modsFolder.mkdirs();
        File[] mods = modsFolder.listFiles();
        if (mods != null) {
            for (Mod mod : modList.getJarMods()) {
                if ((new File(instPath + "mods/" + mod.getFileName())).exists()) {
                    jarMods.add(jarMods.size(), mod);
                }
            }
            for (File modFile : mods) {
                boolean trip = false;
                for (Mod jarMod : modList.getJarMods()) {
                    if (jarMod.getFileName().equals(modFile.getName())) {
                        trip = true;
                    }
                }
                if (!trip && (modFile.getName().endsWith(".jar") || modFile.getName().endsWith(".zip"))) {
                    String modName = modFile.getName();
                    if (modName.contains(".")) {
                        modName = modName.substring(0, modName.lastIndexOf('.'));
                    }
                    jarMods.add(jarMods.size(), new Mod(modFile.getName(), modName, true, new String[]{}, ""));
                }
            }
        }
        modDragDropList.model.clear();
        for (Mod mod : jarMods) {
            modDragDropList.model.addElement(mod);
        }
        modDragDropList.repaint();
    }

    private void refreshLoaderModList() {
        loaderMods = new ArrayList<>();
        File modsFolder = new File(instPath + ".minecraft/mods");
        modsFolder.mkdirs();
        File[] mods = modsFolder.listFiles();
        if (mods != null) {
            for (File modFile : mods) {
                loaderMods.add(loaderMods.size(), LocalMods.getModInfo(instPath, modFile.getName()));
            }
        }
        loaderModDragDropList.model.clear();
        for (Mod mod : loaderMods) {
            loaderModDragDropList.model.addElement(mod);
        }
        loaderModDragDropList.repaint();
    }
}
