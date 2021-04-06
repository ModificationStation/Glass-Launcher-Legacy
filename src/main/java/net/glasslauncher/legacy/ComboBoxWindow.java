package net.glasslauncher.legacy;

import javax.swing.*;
import java.awt.*;

public class ComboBoxWindow extends JDialog {
    private JComboBox<String> comboBox;

    public ComboBoxWindow(Frame owner, String[] contents) {
        super(owner);
        setModal(true);
        setLayout(new GridLayout());
        setResizable(false);

        comboBox = new JComboBox<>();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (String string : contents) {
            model.addElement(string);
        }
        comboBox.setModel(model);
        add(comboBox);

        setPreferredSize(new Dimension(200, 200));
        setMinimumSize(new Dimension(200, 200));
        setLocationRelativeTo(owner);
    }

    public String getValue() {
        return (String) comboBox.getSelectedItem();
    }
}
