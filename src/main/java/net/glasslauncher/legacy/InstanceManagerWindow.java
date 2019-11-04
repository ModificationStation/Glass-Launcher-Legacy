package net.glasslauncher.legacy;

import net.glasslauncher.legacy.components.HintTextField;
import net.glasslauncher.legacy.util.InstanceManager;
import net.glasslauncher.legacy.util.JsonConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class InstanceManagerWindow extends JDialog {
    InstanceManagerWindow(Frame frame) {
        super(frame);
        setModal(true);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle("Instance Manager");
        addWindowListener(new WindowAdapter() {
                              public void windowClosing(WindowEvent we) {
                                      Main.logger.info("Closing instance manager...");
                                      dispose();
                              }
                          }
        );
        setBounds(0, 0, 580, 340);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(0, 0, 580, 340);
        tabbedPane.addTab("Create", makeCreateTab());

        add(tabbedPane);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private Panel makeCreateTab() {
        Panel createPane = new Panel();
        createPane.setBounds(0, 0, 580, 340);
        createPane.setLayout(null);

        // Local Modpack zip install.
        HintTextField installModpackDir = new HintTextField("Paste the path to your Modpack zip here!");
        installModpackDir.setBounds(155, 5, 390, 22);

        JButton installModpackDirButton = new JButton();
        installModpackDirButton.setText("Install Local Modpack");
        installModpackDirButton.setBounds(5, 5, 150, 22);
        installModpackDirButton.addActionListener(event -> {
            InstanceManager.installModpack(installModpackDir.getText());
        });

        // Remote Modpack zip install.
        HintTextField installModpackURL = new HintTextField("Paste the URL to your Modpack zip here!");
        installModpackURL.setBounds(155, 34, 390, 22);
        installModpackDirButton.addActionListener(event -> {
            InstanceManager.installModpack(installModpackURL.getText());
        });

        JButton installModpackURLButton = new JButton();
        installModpackURLButton.setText("Install Modpack from URL");
        installModpackURLButton.setBounds(5, 34, 150, 22);

        HintTextField instanceName = new HintTextField("Instance name");
        instanceName.setBounds(155, 63, 194, 22);

        JComboBox instanceVersion = new JComboBox();
        for (Object version : (new JsonConfig(Main.class.getResource("assets/mcversions.json").toString())).keySet()) {
            instanceVersion.addItem(version);
        }
        instanceVersion.setBounds(351, 63, 194, 22);

        JButton instanceVersionButton = new JButton();
        instanceVersionButton.setText("Create Blank Instance");
        instanceVersionButton.addActionListener((e) -> {
            InstanceManager.createBlankInstance((String) instanceVersion.getSelectedItem(), instanceName.getText());
        });
        instanceVersionButton.setBounds(5, 63, 150, 22);

        createPane.add(installModpackDir);
        createPane.add(installModpackDirButton);
        createPane.add(installModpackURL);
        createPane.add(installModpackURLButton);
        createPane.add(instanceName);
        createPane.add(instanceVersion);
        createPane.add(instanceVersionButton);
        return createPane;
    }
}
