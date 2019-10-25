package net.glasslauncher.legacy;

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

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
