package net.glass.glassl;

import net.glass.glassl.util.JsonConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class OptionsWindow extends Dialog {
    private JsonConfig settings;
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

        instpath = Config.glasspath + "instances/" + instance + "/";
        settings = new JsonConfig(instpath + "/instance_config.json", Config.defaultjson);

        JTabbedPane tabpane = new JTabbedPane();
        tabpane.setPreferredSize(new Dimension(580, 340));

        tabpane.addTab("Settings", makeInstSettings());

        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    settings.set("javaargs", javaargs.getText());
                    settings.set("maxram", maxram.getText());
                    settings.set("minram", minram.getText());
                    settings.set("proxyskin", skinproxy.isSelected());
                    settings.set("proxycape", capeproxy.isSelected());
                    settings.set("proxysound", soundproxy.isSelected());
                    if (settings.saveFile())
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
        javaargs.setText((String) settings.get("javaargs"));
        instsettings.add(javaargs);

        JLabel ramalloclabel = new JLabel("RAM Allocation:");
        ramalloclabel.setBounds(20, 50, 100, 20);
        instsettings.add(ramalloclabel);

        JLabel maxramlabel = new JLabel("Maximum:");
        maxramlabel.setBounds(150, 50, 65, 20);
        instsettings.add(maxramlabel);

        maxram = new JTextField();
        maxram.setBounds(245, 48, 100, 24);
        maxram.setText((String) settings.get("maxram"));
        instsettings.add(maxram);

        JLabel minramlabel = new JLabel("Minimum:");
        minramlabel.setBounds(360, 50, 65, 20);
        instsettings.add(minramlabel);

        minram = new JTextField();
        minram.setBounds(450, 48, 100, 24);
        minram.setText((String) settings.get("minram"));
        instsettings.add(minram);

        JLabel skinproxylabel = new JLabel("Enable Skin Proxy:");
        skinproxylabel.setBounds(20, 96, 120, 20);
        instsettings.add(skinproxylabel);

        skinproxy = new JCheckBox();
        skinproxy.setBounds(150, 97, 20, 20);
        skinproxy.setSelected((Boolean) settings.get("proxyskin"));
        instsettings.add(skinproxy);

        JLabel capeproxylabel = new JLabel("Enable Cape Proxy:");
        capeproxylabel.setBounds(20, 124, 120, 20);
        instsettings.add(capeproxylabel);

        capeproxy = new JCheckBox();
        capeproxy.setBounds(150, 125, 20, 20);
        capeproxy.setSelected((Boolean) settings.get("proxycape"));
        instsettings.add(capeproxy);

        JLabel soundproxylabel = new JLabel("Enable Sound Proxy:");
        soundproxylabel.setBounds(20, 152, 120, 20);
        instsettings.add(soundproxylabel);

        soundproxy = new JCheckBox();
        soundproxy.setBounds(150, 153, 20, 20);
        soundproxy.setSelected((Boolean) settings.get("proxysound"));
        instsettings.add(soundproxy);

        return instsettings;
    }
}
