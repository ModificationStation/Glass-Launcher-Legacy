package net.glasslauncher.legacy;

import net.glasslauncher.legacy.components.HintTextField;
import net.glasslauncher.legacy.util.InstanceManager;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Objects;

class InstanceManagerWindow extends JDialog {
    private JPanel deletePanel = new JPanel();

    InstanceManagerWindow(Frame frame) {
        super(frame);
        setModal(true);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle("Instance Manager");
        addWindowListener(new WindowAdapter() {
                              public void windowClosing(WindowEvent we) {
                                      Main.getLogger().info("Closing instance manager...");
                                      dispose();
                              }
                          }
        );
        setBounds(0, 0, 580, 340);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setBounds(0, 0, 580, 340);
        tabbedPane.addTab("Create", makeCreateTab());
        tabbedPane.addTab("Delete Instance", makeDeleteTab());

        add(tabbedPane);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel makeCreateTab() {
        JPanel createPanel = new JPanel();
        createPanel.setBounds(0, 0, 580, 340);
        createPanel.setLayout(null);

        // Local Modpack zip install.
        HintTextField installModpackDir = new HintTextField("Paste the path to your Modpack zip here!");
        installModpackDir.setBounds(155, 5, 355, 22);

        JButton installModpackDirButton = new JButton();
        installModpackDirButton.setText("Install Local Modpack");
        installModpackDirButton.setBounds(5, 5, 150, 22);
        installModpackDirButton.addActionListener(event -> {
            ProgressWindow progressWindow = new ProgressWindow(this, "Installing Local Modpack...");
            (new Thread(() -> {
                InstanceManager.installModpack(installModpackDir.getText(), progressWindow);
                progressWindow.dispose();
            })).start();
            if (progressWindow.isDisplayable()) {
                progressWindow.setVisible(true);
            }
        });

        JButton instanceZipSelectButton = new JButton();
        instanceZipSelectButton.setText("...");
        instanceZipSelectButton.addActionListener(event -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Modpack Zip (*.zip)", "zip"));
            fileChooser.showOpenDialog(this);
            File file = fileChooser.getSelectedFile();
            installModpackDir.setText(file.getAbsolutePath());
        });
        instanceZipSelectButton.setBounds(515, 5, 30, 22);

        // Remote Modpack zip install.
        HintTextField installModpackURL = new HintTextField("Paste the URL to your Modpack zip here!");
        installModpackURL.setBounds(155, 34, 390, 22);

        JButton installModpackURLButton = new JButton();
        installModpackURLButton.setText("Install Modpack from URL");
        installModpackURLButton.setBounds(5, 34, 150, 22);
        installModpackURLButton.addActionListener(event -> {
            ProgressWindow progressWindow = new ProgressWindow(this, "Installing Remote Modpack...");
            (new Thread(() -> {
                InstanceManager.installModpack(installModpackURL.getText(), progressWindow);
                progressWindow.dispose();
            })).start();
            if (progressWindow.isDisplayable()) {
                progressWindow.setVisible(true);
            }
        });

        // Make blank instance
        HintTextField instanceName = new HintTextField("Instance name");
        instanceName.setBounds(155, 63, 194, 22);

        JComboBox<String> instanceVersion = new JComboBox<>();
        for (String version : Config.getMcVersions().getClient().keySet()) {
            instanceVersion.addItem(version);
        }
        instanceVersion.setBounds(351, 63, 194, 22);

        JButton instanceVersionButton = new JButton();
        instanceVersionButton.setText("Create Blank Instance");
        instanceVersionButton.addActionListener((e) -> {
            ProgressWindow progressWindow = new ProgressWindow(this, "Creating New Instance...");
            (new Thread(() -> {
                InstanceManager.createBlankInstance((String) instanceVersion.getSelectedItem(), instanceName.getText(), progressWindow);
                progressWindow.dispose();
            })).start();
            if (progressWindow.isDisplayable()) {
                progressWindow.setVisible(true);
            }
        });
        instanceVersionButton.setBounds(5, 63, 150, 22);

        createPanel.add(installModpackDir);
        createPanel.add(installModpackDirButton);
        createPanel.add(instanceZipSelectButton);
        createPanel.add(installModpackURL);
        createPanel.add(installModpackURLButton);
        createPanel.add(instanceName);
        createPanel.add(instanceVersion);
        createPanel.add(instanceVersionButton);
        return createPanel;
    }

    private JScrollPane makeDeleteTab() {
        deletePanel.setLayout(new BoxLayout(deletePanel, BoxLayout.Y_AXIS));

        updateInstanceList();

        return new JScrollPane(deletePanel);
    }

    private void updateInstanceList() {
        (new File(Config.GLASS_PATH + "instances")).mkdirs();
        deletePanel.removeAll();
        deletePanel.repaint();
        for (File instance : (Objects.requireNonNull(new File(Config.GLASS_PATH + "instances").listFiles()))) {
            if (instance.isDirectory()) {
                JButton deleteButton = new JButton();
                deleteButton.setText("Delete \"" + instance.getName() + "\".");
                deleteButton.setMinimumSize(new Dimension(540, 22));
                deleteButton.setMaximumSize(new Dimension(540, 22));
                deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                deleteButton.addActionListener((e) -> {
                    try {
                        FileUtils.deleteDirectory(instance);
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
