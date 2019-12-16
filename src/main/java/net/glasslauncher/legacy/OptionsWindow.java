package net.glasslauncher.legacy;

import net.glasslauncher.jsontemplate.InstanceConfig;
import net.glasslauncher.jsontemplate.Mod;
import net.glasslauncher.jsontemplate.ModList;
import net.glasslauncher.legacy.components.DragDropList;
import net.glasslauncher.legacy.util.JsonConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class OptionsWindow extends JDialog {
    private InstanceConfig instanceConfig;
    /*
    modList JSON:
    {
        "mod file name": {
            "name": "the mods name",
            "type": <0-2>,
            "enabled": <true|false>
        }
    }
     */
    private ModList modList;
    private ArrayList<Mod> jarMods;
    private ArrayList<Mod> loaderMods;

    private String instpath;

    private JTextField javaargs;
    private JTextField minram;
    private JTextField maxram;
    private JCheckBox skinproxy;
    private JCheckBox capeproxy;
    private JCheckBox soundproxy;

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

                    for (Object modObj : jarMods.toArray()) {
                        Mod mod;
                        try {
                            mod = (Mod) modObj;
                            modList.getJarMods().put(mod.getFileName(), mod);
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

        return instsettings;
    }

    private JPanel makeMods() {
        JPanel modsPanel = new JPanel();
        modsPanel.setLayout(null);

        jarMods = new ArrayList<>();
        File modsFile = new File(instpath + "mods");
        modsFile.mkdirs();
        Set<String> keys = modList.getJarMods().keySet();
        for (String key : keys) {
            jarMods.add(modList.getJarMods().get(key));
        }
        JScrollPane modListScroll = new JScrollPane();
        DragDropList modList = new DragDropList(jarMods);
        modListScroll.setBounds(0, 0, 200, 200);
        modListScroll.setViewportView(modList);

        modsPanel.add(modListScroll);

        return modsPanel;
    }
}
