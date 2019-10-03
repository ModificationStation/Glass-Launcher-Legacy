package net.glass.glassl.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class HintTextField extends JTextField {
    private final Color defaultColor;

    public HintTextField(String hint) {
        this(hint, Color.black, Color.gray);
    }

    public HintTextField(String hint, Color defaultColor) {
        this(hint, defaultColor, Color.gray);
    }

    public HintTextField(String hint, Color defaultColor, Color hintColor) {
        this.defaultColor = defaultColor;
        this.setText(hint);
        this.setToolTipText(hint);
        this.setForeground(hintColor);
        this.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (getForeground() == hintColor) {
                    setText("");
                    setForeground(defaultColor);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(hint);
                    setForeground(hintColor);
                }
            }
        });
    }

    public void setText(String text) {
        super.setText(text);
        setForeground(defaultColor);
    }
}
