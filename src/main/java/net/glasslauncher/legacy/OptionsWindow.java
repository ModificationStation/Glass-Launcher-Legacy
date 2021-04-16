package net.glasslauncher.legacy;

import com.google.gson.Gson;
import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.JsonConfig;
import net.glasslauncher.legacy.components.*;
import net.glasslauncher.legacy.components.handlers.RepoModTableModel;
import net.glasslauncher.legacy.components.templates.DetailsPanel;
import net.glasslauncher.legacy.jsontemplate.*;
import net.glasslauncher.legacy.mc.LocalMods;
import net.glasslauncher.legacy.util.InstanceManager;
import net.glasslauncher.repo.api.mod.RepoReader;
import net.glasslauncher.repo.api.mod.jsonobj.ModValues;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipFile;

public class OptionsWindow extends JDialog {
    private Frame parent;
    private JPanel panel;
    private InstanceConfig instanceConfig;
    private ModList modList;
    private ArrayList<Mod> jarMods;
    private DragDropList modDragDropList;

    private DragDropList loaderModDragDropList;
    private ArrayList<Mod> loaderMods;

    private String instPath;
    private String instName;
    private ModValues validValues;
    private String typeFilter = "";
    private String categoryFilter = "";
    private boolean modCompatChecked = true;

    private JTextFieldFancy javaargs;
    private JTextFieldFancy minram;
    private JTextFieldFancy maxram;
    private JCheckBox skinproxy;
    private JCheckBox capeproxy;
    private JCheckBox soundproxy;
    private JCheckBox loginproxy;
    private JCheckBox disableIntermediary;
    private JComboBox<String> instanceVersion;

    private boolean ignoreInstanceVersion = false;

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
        instPath = CommonConfig.getGlassPath() + "instances/" + instance + "/";
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
                    if (jarMods.size() > 0 && !instanceConfig.isDisableIntermediary()) {
                        int response = JOptionPane.showConfirmDialog(parent, "Jar mods detected. Do you want to disable intermediary mappings?\nThis will break fabric mods, but will unbreak your jar mods.", "Warning", JOptionPane.YES_NO_OPTION);
                        if (response == JOptionPane.YES_OPTION) {
                            disableIntermediary.setSelected(true);
                        }
                    }
                    if (!modCompatChecked) {
                        int response = JOptionPane.showConfirmDialog(parent, "Jar mod compatiblity has not been checked!\nAre you sure you don't want to run a check to see if any mods conflict?");
                        if (response != JOptionPane.YES_OPTION) {
                            checkModCompat();
                        }
                    }
                    instanceConfig.setJavaArgs(javaargs.getText());
                    instanceConfig.setMaxRam(maxram.getText());
                    instanceConfig.setMinRam(minram.getText());
                    instanceConfig.setProxySkin(skinproxy.isSelected());
                    instanceConfig.setProxyCape(capeproxy.isSelected());
                    instanceConfig.setProxySound(soundproxy.isSelected());
                    instanceConfig.setProxyLogin(loginproxy.isSelected());
                    instanceConfig.setDisableIntermediary(disableIntermediary.isSelected());
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

        JLabelFancy disableIntermediaryLabel = new JLabelFancy("Disable Intermediary (Can break jar mods if off):");
        disableIntermediaryLabel.setBounds(20, 208, 250, 20);
        instsettings.add(disableIntermediaryLabel);

        disableIntermediary = new JCheckBox();
        disableIntermediary.setOpaque(false);
        disableIntermediary.setBounds(280, 209, 20, 20);
        disableIntermediary.setSelected(instanceConfig.isDisableIntermediary());
        instsettings.add(disableIntermediary);

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
            modCompatChecked = false;
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
            modCompatChecked = false;
        });
        addModsButton.setBounds(20, 294, 200, 22);

        JButtonScalingFancy removeModsButton = new JButtonScalingFancy();
        removeModsButton.setText("Remove Selected Mods");
        removeModsButton.setForeground(new Color(185, 0, 0));
        removeModsButton.addActionListener(event -> {
            for (Mod mod : modDragDropList.getSelectedValuesList()) {
                (new File(instPath + "mods/" + mod.getFileName())).delete();
            }
            refreshJarModList();
//            modCompatChecked = false;
        });
        removeModsButton.setBounds(20, 326, 200, 22);

        JButtonScalingFancy checkCompatButton = new JButtonScalingFancy();
        checkCompatButton.setText("Check Compatibility");
        checkCompatButton.addActionListener(event -> {
            checkModCompat();
            modCompatChecked = true;
        });
        checkCompatButton.setBounds(20, 358, 200, 22);

        modsPanel.add(modListScroll);
        modsPanel.add(toggleModsButton);
        modsPanel.add(applyModsButton);
        modsPanel.add(addModsButton);
        modsPanel.add(removeModsButton);
        modsPanel.add(checkCompatButton);

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
            System.out.println(loaderModDragDropList);
            for (Mod mod : loaderModDragDropList.getSelectedValuesList()) {
                (new File(instPath + ".minecraft/mods/" + mod.getFileName())).delete();
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
        tableModel = new RepoModTableModel();
        table = new JDetailsTable(parent, tableModel, instName);
        table.setFillsViewportHeight(true);

        TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) table.getRowSorter();
        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                return ignoreInstanceVersion || entry.getStringValue(4).equals(instanceConfig.getVersion());
            }
        });
        sorter.setSortable(5, false);
        sorter.setSortable(6, false);
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col == 5) {
                    ComboBoxWindow comboBoxWindow = new ComboBoxWindow(parent, validValues.getTypes());
                    comboBoxWindow.setVisible(true);
                    if (comboBoxWindow.getValue() != null) {
                        typeFilter = comboBoxWindow.getValue();
                        table.getRowSorter().allRowsChanged();
                    }
                    else {
                        typeFilter = "";
                    }
                }
                else if (col == 6) {
                    ComboBoxWindow comboBoxWindow = new ComboBoxWindow(parent, validValues.getCategories());
                    comboBoxWindow.setVisible(true);
                    if (comboBoxWindow.getValue() != null) {
                        categoryFilter = comboBoxWindow.getValue();
                        table.getRowSorter().allRowsChanged();
                    }
                    else {
                        categoryFilter = "";
                    }
                }
                else {
                    super.mouseClicked(e);
                }
                System.out.println(typeFilter);
                System.out.println(categoryFilter);
            }
        });

        new Thread(() -> {
            try {
                validValues = RepoReader.getValidValues().getValidValues();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Async cause otherwise options freezes when opening for 1-5 seconds.
        new Thread(this::refreshRepo).start();

        JScrollPane modListScroll = new JScrollPane(table);
        if (!Config.getLauncherConfig().isThemeDisabled()) {
            modListScroll.getViewport().setBackground(new Color(52, 52, 52));
        }
        modListScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        modRepoPanel.add(modListScroll, BorderLayout.CENTER);

        return modRepoPanel;
    }

    private void refreshRepo() {
        try {
            tableModel.setMods(Arrays.asList(RepoReader.getMods()));
            tableModel.fireTableDataChanged();
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

    private void checkModCompat() {
        HashMap<Mod, HashMap<Mod, ModCompatInfo>> incompatibleMods = new HashMap<>();
        Object[] objArray = modDragDropList.model.toArray();
        ArrayList<Mod> theRealArray = new ArrayList<Mod>(){{Arrays.stream(objArray).forEach((obj) -> add((Mod) obj)); }};
        for (Mod mod : theRealArray) {
            try {
                File modPath = new File(instPath, "mods/" + mod.getFileName());
                ZipFile modFile = new ZipFile(modPath, ZipFile.OPEN_READ);
                ArrayList<String> modFileNames = new ArrayList<>();
                modFile.stream().forEach(zipEntry -> modFileNames.add(zipEntry.getName()));
                for (Mod checkedMod : theRealArray) {
                    if (mod == checkedMod) {
                        continue;
                    }
                    try {
                        File checkedModPath = new File(instPath, "mods/" + checkedMod.getFileName());
                        ZipFile checkedModFile = new ZipFile(checkedModPath, ZipFile.OPEN_READ);
                        checkedModFile.stream().forEach(zipEntry -> {
                            if (modFileNames.contains(zipEntry.getName())) {
                                incompatibleMods.computeIfAbsent(mod, mod1 -> new HashMap<>());
                                if (incompatibleMods.get(mod).get(checkedMod) == null) {
                                    incompatibleMods.get(mod).put(checkedMod, new ModCompatInfo(checkedMod));
                                }
                                incompatibleMods.get(mod).get(checkedMod).getClasses().add(new ClassInfo(zipEntry.getName()));
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println((new Gson()).toJson(incompatibleMods));
        new ModCompatWindow(parent, instName, incompatibleMods).setVisible(true);
    }
}
