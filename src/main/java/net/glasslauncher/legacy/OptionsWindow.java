package net.glasslauncher.legacy;

import com.google.gson.Gson;
import net.glasslauncher.jsontemplate.InstanceConfig;
import net.glasslauncher.jsontemplate.Mod;
import net.glasslauncher.jsontemplate.ModList;
import net.glasslauncher.legacy.components.DragDropList;
import net.glasslauncher.legacy.util.InstanceManager;
import net.glasslauncher.legacy.util.JsonConfig;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.JarEntry;

public class OptionsWindow extends JDialog {
    private InstanceConfig instanceConfig;
    private ModList modList;
    private ArrayList<Mod> jarMods;
    private ArrayList<Mod> loaderMods;
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
     * @param frame Frame object to block while open.
     * @param instance Target instance.
     */
    public OptionsWindow(Frame frame, String instance) {
        super(frame);
        setModal(true);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle("Instance Options");

        instName = instance;
        instpath = Config.getGlassPath() + "instances/" + instance + "/";
        instanceConfig = (InstanceConfig) JsonConfig.loadConfig(instpath + "instance_config.json", InstanceConfig.class);
        if (instanceConfig == null) {
            instanceConfig = new InstanceConfig(instpath + "instance_config.json");
        }
        modList = (ModList) JsonConfig.loadConfig(instpath + "mods/mods.json", ModList.class);
        if (modList == null) {
            modList = new ModList(instpath + "mods/mods.json");
        }

        JTabbedPane tabpane = new JTabbedPane();
        tabpane.setPreferredSize(new Dimension(837, 448 - tabpane.getInsets().top - tabpane.getInsets().bottom));

        tabpane.addTab("Settings", makeInstSettings());
        tabpane.addTab("Mods", makeMods());

        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    instanceConfig.setJavaArgs(javaargs.getText());
                    instanceConfig.setMinRam(maxram.getText());
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

        add(tabpane);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Makes the things that do the thing for the instance JSON.
     *
     * @return The panel object containing all created components.
     */
    private Panel makeInstSettings() {
        Panel instsettings = new Panel();
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
        skinproxy.setBounds(150, 97, 20, 20);
        skinproxy.setSelected(instanceConfig.isProxySkin());
        instsettings.add(skinproxy);

        JLabel capeproxylabel = new JLabel("Enable Cape Proxy:");
        capeproxylabel.setBounds(20, 124, 120, 20);
        instsettings.add(capeproxylabel);

        capeproxy = new JCheckBox();
        capeproxy.setBounds(150, 125, 20, 20);
        capeproxy.setSelected(instanceConfig.isProxyCape());
        instsettings.add(capeproxy);

        JLabel soundproxylabel = new JLabel("Enable Sound Proxy:");
        soundproxylabel.setBounds(20, 152, 120, 20);
        instsettings.add(soundproxylabel);

        soundproxy = new JCheckBox();
        soundproxy.setBounds(150, 153, 20, 20);
        soundproxy.setSelected(instanceConfig.isProxySound());
        instsettings.add(soundproxy);

        JLabel loginproxylabel = new JLabel("Enable Login Proxy:");
        loginproxylabel.setBounds(20, 180, 120, 20);
        instsettings.add(loginproxylabel);

        loginproxy = new JCheckBox();
        loginproxy.setBounds(150, 181, 20, 20);
        loginproxy.setSelected(instanceConfig.isProxyLogin());
        instsettings.add(loginproxy);

        return instsettings;
    }

    private JPanel makeMods() {
        JPanel modsPanel = new JPanel();
        modsPanel.setLayout(null);

        jarMods = new ArrayList<>();
        JScrollPane modListScroll = new JScrollPane();
        modDragDropList = new DragDropList(jarMods);
        refreshModList();
        modListScroll.setBounds(0, 0, 200, 200);
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
        toggleModsButton.setBounds(0, 210, 200, 22);

        JButton applyModsButton = new JButton();
        applyModsButton.setText("Apply Current Mod Configuration");
        applyModsButton.addActionListener(event -> {
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
            InstanceManager.addMods(instName, modDragDropList.model);
        });
        applyModsButton.setBounds(0, 242, 200, 22);

        JButton addModsButton = new JButton();
        addModsButton.setText("Add Mods");
        addModsButton.addActionListener(event -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Mod Zip (*.zip;*.jar)", "zip", "jar"));
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.showOpenDialog(this);
            File[] files = fileChooser.getSelectedFiles();
            try {
                for (File file : files) {
                    FileUtils.copyFile(file, new File(instpath + "mods/" + file.getName()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshModList();
        });
        addModsButton.setBounds(0, 274, 200, 22);

        JButton removeModsButton = new JButton();
        removeModsButton.setText("Remove Selected Mods");
        removeModsButton.addActionListener(event -> {
            for (Object modObj : modDragDropList.getSelectedValuesList()) {
                Mod mod = (Mod) modObj;
                (new File(instpath + "mods/" + mod.getFileName())).delete();
            }
            refreshModList();
        });
        removeModsButton.setBounds(0, 306, 200, 22);

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
