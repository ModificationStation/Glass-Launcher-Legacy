package net.glasslauncher.legacy;

import net.glasslauncher.legacy.components.handlers.JavaInstallTableModel;
import net.glasslauncher.legacy.util.JavaFinder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JavaInstallSelectWindow extends JDialog {
    private JTable table;

    public JavaInstallSelectWindow(Window frame) {
        super(frame);

        setPreferredSize(new Dimension(350, 200));

        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle("Java Install Detector");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JavaInstallTableModel javaInstallTableModel = new JavaInstallTableModel();
        javaInstallTableModel.setData(JavaFinder.findJavaPaths());
        table = new JTable(javaInstallTableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setMinWidth(300);
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
        table.getActionMap().put("Enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    dispose();
                }
            }
        });

        panel.add(table);
        add(panel);
        pack();
        setLocationRelativeTo(frame);

        setVisible(true);
    }

    public String getJava() {
        return (String) table.getModel().getValueAt(table.getSelectedRow(), 0);
    }
}
