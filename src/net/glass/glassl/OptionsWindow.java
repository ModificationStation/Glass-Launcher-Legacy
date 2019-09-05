package net.glass.glassl;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class OptionsWindow extends Dialog {
    private JsonObject settings;
    private String instpath;

    private Checkbox skinproxy;
    private Checkbox capeproxy;
    private Checkbox soundproxy;

    private HashMap prettyprint = new HashMap(){{
        put("PRETTY_PRINT", true);
        put("TYPE", false);
    }};

    public OptionsWindow(Frame frame, String instance) {
        super(frame);
        setModal(true);
        setLayout(new GridLayout());
        setResizable(false);

        instpath = Config.instpath + "instances/" + instance + "/";
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
                                  settings.put("proxyskin", skinproxy.getState());
                                  settings.put("proxycape", capeproxy.getState());
                                  settings.put("proxysound", soundproxy.getState());
                                  try (PrintStream out = new PrintStream(new FileOutputStream(instpath + "instance_config.json"))) {
                                      out.print(JsonWriter.objectToJson(settings, prettyprint));
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

        Label javaargslabel = new Label("Java Arguments:");
        javaargslabel.setBounds(20, 22, 100, 20);
        instsettings.add(javaargslabel);

        TextField javaargs = new TextField();
        javaargs.setBounds(150, 20, 400, 24);
        javaargs.setText("Defunct");
        javaargs.setEditable(false);
        javaargs.setForeground(Color.gray);
        instsettings.add(javaargs);

        Label ramalloclabel = new Label("RAM Allocation:");
        ramalloclabel.setBounds(20, 50, 100, 20);
        instsettings.add(ramalloclabel);

        Label maxramlabel = new Label("Maximum:");
        maxramlabel.setBounds(150, 50, 65, 20);
        instsettings.add(maxramlabel);

        TextField maxram = new TextField();
        maxram.setBounds(245, 48, 100, 24);
        maxram.setText(Runtime.getRuntime().maxMemory()/1024/1024 + "M");
        maxram.setEditable(false);
        maxram.setForeground(Color.gray);
        instsettings.add(maxram);

        Label minramlabel = new Label("Minimum:");
        minramlabel.setBounds(360, 50, 65, 20);
        instsettings.add(minramlabel);

        TextField minram = new TextField();
        minram.setBounds(450, 48, 100, 24);
        minram.setText(Runtime.getRuntime().totalMemory()/1024/1024 + "M");
        minram.setEditable(false);
        minram.setForeground(Color.gray);
        instsettings.add(minram);

        Label skinproxylabel = new Label("Enable Skin Proxy:");
        skinproxylabel.setBounds(20, 96, 120, 20);
        instsettings.add(skinproxylabel);

        skinproxy = new Checkbox();
        skinproxy.setBounds(150, 97, 20, 20);
        skinproxy.setState((Boolean) settings.get("proxyskin"));
        instsettings.add(skinproxy);

        Label capeproxylabel = new Label("Enable Cape Proxy:");
        capeproxylabel.setBounds(20, 124, 120, 20);
        instsettings.add(capeproxylabel);

        capeproxy = new Checkbox();
        capeproxy.setBounds(150, 125, 20, 20);
        capeproxy.setState((Boolean) settings.get("proxycape"));
        instsettings.add(capeproxy);

        Label soundproxylabel = new Label("Enable Sound Proxy:");
        soundproxylabel.setBounds(20, 152, 120, 20);
        instsettings.add(soundproxylabel);

        soundproxy = new Checkbox();
        soundproxy.setBounds(150, 153, 20, 20);
        soundproxy.setState((Boolean) settings.get("proxysound"));
        instsettings.add(soundproxy);

        return instsettings;
    }
}
