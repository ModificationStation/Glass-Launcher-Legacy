package net.glass.glassl;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class OptionsWindow extends Dialog {
    private JsonObject settings;
    private String instpath;

    private JCheckBox skinproxy;
    private JCheckBox capeproxy;
    private JCheckBox soundproxy;

    public OptionsWindow(Frame frame, String instance) {
        super(frame);
        setModal(true);
        setLayout(new GridLayout());
        setResizable(false);

        instpath = Config.glasspath + "instances/" + instance + "/";
        try {
            settings = (JsonObject) JsonReader.jsonToJava(new Scanner(new FileInputStream(instpath + "/instance_config.json"), "UTF-8").useDelimiter("\\A").next());
        }
        catch (Exception e) {
            settings = (JsonObject) JsonReader.jsonToJava(Config.defaultjson);
        }

        JTabbedPane tabpane = new JTabbedPane();
        tabpane.setPreferredSize(new Dimension(580, 340));

        tabpane.addTab("Settings", makeInstSettings());

        addWindowListener(new WindowAdapter() {
                              public void windowClosing(WindowEvent we) {
                                  settings.put("proxyskin", skinproxy.isSelected());
                                  settings.put("proxycape", capeproxy.isSelected());
                                  settings.put("proxysound", soundproxy.isSelected());
                                  try (PrintStream out = new PrintStream(new FileOutputStream(instpath + "instance_config.json"))) {
                                      out.print(JsonWriter.objectToJson(settings, Config.prettyprint));
                                  }
                                  catch (FileNotFoundException e) {
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

    private Panel makeInstSettings() {
        Panel instsettings = new Panel();
        instsettings.setLayout(null);

        JLabel javaargslabel = new JLabel("Java Arguments:");
        javaargslabel.setBounds(20, 22, 100, 20);
        instsettings.add(javaargslabel);

        JTextField javaargs = new JTextField();
        javaargs.setBounds(150, 20, 400, 24);
        javaargs.setText("Defunct");
        javaargs.setEditable(false);
        javaargs.setForeground(Color.gray);
        instsettings.add(javaargs);

        JLabel ramalloclabel = new JLabel("RAM Allocation:");
        ramalloclabel.setBounds(20, 50, 100, 20);
        instsettings.add(ramalloclabel);

        JLabel maxramlabel = new JLabel("Maximum:");
        maxramlabel.setBounds(150, 50, 65, 20);
        instsettings.add(maxramlabel);

        JTextField maxram = new JTextField();
        maxram.setBounds(245, 48, 100, 24);
        maxram.setText(Runtime.getRuntime().maxMemory()/1024/1024 + "M");
        maxram.setEditable(false);
        maxram.setForeground(Color.gray);
        instsettings.add(maxram);

        JLabel minramlabel = new JLabel("Minimum:");
        minramlabel.setBounds(360, 50, 65, 20);
        instsettings.add(minramlabel);

        JTextField minram = new JTextField();
        minram.setBounds(450, 48, 100, 24);
        minram.setText(Runtime.getRuntime().totalMemory()/1024/1024 + "M");
        minram.setEditable(false);
        minram.setForeground(Color.gray);
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
