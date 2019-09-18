package net.glass.glassl.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class HintTextField extends JTextField {
    public HintTextField(String hint) {
        this.setText(hint);
        this.setToolTipText(hint);
        this.setForeground(Color.gray);
        this.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (getForeground() == Color.gray) {
                    setText("");
                    setForeground(Color.black);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(hint);
                    setForeground(Color.gray);
                }
            }
        });
    }
}
