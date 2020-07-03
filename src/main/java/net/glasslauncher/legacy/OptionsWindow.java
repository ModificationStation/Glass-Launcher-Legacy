package net.glasslauncher.legacy;

import net.glasslauncher.jsontemplate.InstanceConfig;
import net.glasslauncher.jsontemplate.Mod;
import net.glasslauncher.jsontemplate.ModList;
import net.glasslauncher.legacy.components.DragDropList;
import net.glasslauncher.legacy.util.InstanceManager;
import net.glasslauncher.legacy.util.JsonConfig;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class OptionsWindow extends JDialog {
    private JPanel panel;
    private InstanceConfig instanceConfig;
    private ModList modList;
    private ArrayList<Mod> jarMods;
    DragDropList modDragDropList;

    private String instpath;
    private String instName;

    private JTextField javaargs;
    private JTextField minram;
    private JTextField maxram;
    private JCheckBox skinproxy;
    private JCheckBox capeproxy;
    private JCheckBox soundproxy;
    private JCheckBox loginproxy;

    /**
     * Sets up options window for given instance.
     * @param panel Frame object to block while open.
     * @param instance Target instance.
     */
    public OptionsWindow(Frame panel, String instance) {
        super(panel);
        setModal(true);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle("Instance Options");
        this.panel = new JPanel();
        add(this.panel);

        instName = instance;
        instpath = Config.GLASS_PATH + "instances/" + instance + "/";
        if (!(new File(instpath)).exists()) {
            JOptionPane.showMessageDialog(this, "Selected instance does not exist, or one hasn't been selected.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        instanceConfig = (InstanceConfig) JsonConfig.loadConfig(instpath + "instance_config.json", InstanceConfig.class);
        if (instanceConfig == null) {
            instanceConfig = new InstanceConfig(instpath + "instance_config.json");
        }
        modList = (ModList) JsonConfig.loadConfig(instpath + "mods/mods.json", ModList.class);
        if (modList == null) {
            modList = new ModList(instpath + "mods/mods.json");
        }

        JTabbedPane tabpane = new JTabbedPane();
        tabpane.setOpaque(false);
        tabpane.setPreferredSize(new Dimension(837, 448 - tabpane.getInsets().top - tabpane.getInsets().bottom));

        tabpane.addTab("Settings", makeInstSettings());
        tabpane.addTab("Mods", makeMods());

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
                    dispose();
                  }
            }
        );

        this.panel.add(tabpane);
        pack();
        setLocationRelativeTo(null);
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

        JLabel javaargslabel = new JLabel("Java Arguments:");
        javaargslabel.setBounds(20, 22, 100, 20);
        instsettings.add(javaargslabel);

        javaargs = new JTextField();
        javaargs.setBounds(150, 20, 400, 24);
        javaargs.setText(instanceConfig.getJavaArgs());
        instsettings.add(javaargs);

        JLabel ramalloclabel = new JLabel("RAM Allocation:");
        ramalloclabel.setBounds(20, 50, 100, 20);
        instsettings.add(ramalloclabel);

        JLabel maxramlabel = new JLabel("Maximum:");
        maxramlabel.setBounds(150, 50, 65, 20);
        instsettings.add(maxramlabel);

        maxram = new JTextField();
        maxram.setBounds(245, 48, 100, 24);
        maxram.setText(instanceConfig.getMaxRam());
        instsettings.add(maxram);

        JLabel minramlabel = new JLabel("Minimum:");
        minramlabel.setBounds(360, 50, 65, 20);
        instsettings.add(minramlabel);

        minram = new JTextField();
        minram.setBounds(450, 48, 100, 24);
        minram.setText(instanceConfig.getMinRam());
        instsettings.add(minram);

        JLabel skinproxylabel = new JLabel("Enable Skin Proxy:");
        skinproxylabel.setBounds(20, 96, 120, 20);
        instsettings.add(skinproxylabel);

        skinproxy = new JCheckBox();
        skinproxy.setOpaque(false);
        skinproxy.setBounds(150, 97, 20, 20);
        skinproxy.setSelected(instanceConfig.isProxySkin());
        instsettings.add(skinproxy);

        JLabel capeproxylabel = new JLabel("Enable Cape Proxy:");
        capeproxylabel.setBounds(20, 124, 120, 20);
        instsettings.add(capeproxylabel);

        capeproxy = new JCheckBox();
        capeproxy.setOpaque(false);
        capeproxy.setBounds(150, 125, 20, 20);
        capeproxy.setSelected(instanceConfig.isProxyCape());
        instsettings.add(capeproxy);

        JLabel soundproxylabel = new JLabel("Enable Sound Proxy:");
        soundproxylabel.setBounds(20, 152, 120, 20);
        instsettings.add(soundproxylabel);

        soundproxy = new JCheckBox();
        soundproxy.setOpaque(false);
        soundproxy.setBounds(150, 153, 20, 20);
        soundproxy.setSelected(instanceConfig.isProxySound());
        instsettings.add(soundproxy);

        JLabel loginproxylabel = new JLabel("Enable Login Proxy:");
        loginproxylabel.setBounds(20, 180, 120, 20);
        instsettings.add(loginproxylabel);

        loginproxy = new JCheckBox();
        loginproxy.setOpaque(false);
        loginproxy.setBounds(150, 181, 20, 20);
        loginproxy.setSelected(instanceConfig.isProxyLogin());
        instsettings.add(loginproxy);

        return instsettings;
    }

    private JPanel makeMods() {
        JPanel modsPanel = new JPanel();
        modsPanel.setOpaque(false);
        modsPanel.setLayout(null);

        jarMods = new ArrayList<>();
        JScrollPane modListScroll = new JScrollPane();
        modDragDropList = new DragDropList(jarMods);
        refreshModList();
        modListScroll.setBounds(20, 20, 200, 200);
        modListScroll.setViewportView(modDragDropList);

        JButton toggleModsButton = new JButton();
        toggleModsButton.setText("Toggle Selected Mods");
        toggleModsButton.addActionListener(event -> {
            for (Object modObj : modDragDropList.getSelectedValuesList()) {
                if (modObj instanceof Mod) {
                    Mod mod = (Mod) modObj;
                    mod.setEnabled(!mod.isEnabled());
                } else {
                    Main.getLogger().severe("Mod object not instance of Mod!");
                }
            }
            modDragDropList.repaint();
        });
        toggleModsButton.setBounds(20, 230, 200, 22);

        JButton applyModsButton = new JButton();
        applyModsButton.setText("Apply Current Mod Configuration");
        applyModsButton.addActionListener(event -> {
            ProgressWindow progressWindow = new ProgressWindow(this, "Applying Mods");
            Thread thread = new Thread(() -> {
                progressWindow.setProgressMax(2);
                progressWindow.setProgress(0);
                progressWindow.setProgressText("Setting up");
                File vanillaJar = new File(instpath + ".minecraft/bin/minecraft_vanilla.jar");
                File moddedJar = new File(instpath + ".minecraft/bin/minecraft.jar");
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

        JButton addModsButton = new JButton();
        addModsButton.setText("Add Mods");
        addModsButton.addActionListener(event -> {
            FileDialog fileChooser = new FileDialog(this, "Select Mod");
            fileChooser.setFilenameFilter((e, str) -> {
                return str.endsWith(".jar") || str.endsWith(".zip");
            });
            fileChooser.setMultipleMode(true);
            fileChooser.setVisible(true);
            File[] files = fileChooser.getFiles();
            try {
                for (File file : files) {
                    Files.copy(file.toPath(), new File(instpath + "mods/" + file.getName()).toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {}
            refreshModList();
        });
        addModsButton.setBounds(20, 294, 200, 22);

        JButton removeModsButton = new JButton();
        removeModsButton.setText("Remove Selected Mods");
        removeModsButton.setForeground(new Color(81, 0, 0));
        removeModsButton.addActionListener(event -> {
            for (Object modObj : modDragDropList.getSelectedValuesList()) {
                Mod mod = (Mod) modObj;
                (new File(instpath + "mods/" + mod.getFileName())).delete();
            }
            refreshModList();
        });
        removeModsButton.setBounds(20, 326, 200, 22);

        modsPanel.add(modListScroll);
        modsPanel.add(toggleModsButton);
        modsPanel.add(applyModsButton);
        modsPanel.add(addModsButton);
        modsPanel.add(removeModsButton);

        return modsPanel;
    }

    private void refreshModList() {
        jarMods = new ArrayList<>();
        File modsFolder = new File(instpath + "mods");
        modsFolder.mkdirs();
        File[] mods = modsFolder.listFiles();
        if (mods != null) {
            for (Mod mod : modList.getJarMods()) {
                if ((new File(instpath + "mods/" + mod.getFileName())).exists()) {
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
                    jarMods.add(jarMods.size(), new Mod(modFile.getName(), modName, 0, true));
                }
            }
        }
        modDragDropList.model.clear();
        for (Mod mod : jarMods) {
            modDragDropList.model.addElement(mod);
        }
        modDragDropList.repaint();
    }
}
