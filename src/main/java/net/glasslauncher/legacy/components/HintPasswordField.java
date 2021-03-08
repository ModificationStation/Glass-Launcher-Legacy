package net.glasslauncher.legacy.components;

import javax.swing.JPasswordField;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class HintPasswordField extends JPasswordField {
    private final Color defaultColor;
    private final Color hintColor;
    private final String hint;

    public HintPasswordField(String hint) {
        this(hint, Color.black, Color.gray);
    }

    public HintPasswordField(String hint, Color defaultColor, Color hintColor) {
        this.defaultColor = defaultColor;
        this.hintColor = hintColor;
        this.hint = hint;
        this.setText(hint);
        this.setToolTipText(hint);
        this.setForeground(hintColor);
        this.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (getForeground() == hintColor) {
                    HintPasswordField.super.setText("");
                    setEchoChar('•');
                    setForeground(defaultColor);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (String.valueOf(getPassword()).isEmpty()) {
                    setText(hint);
                    setEchoChar((char) 0);
                    setForeground(hintColor);
                }
            }
        });
    }

    public void setText(String text) {
        if (text.isEmpty()) {
            setText(hint);
            setEchoChar((char) 0);
            setForeground(hintColor);
        }
        else {
            super.setText(text);
            setEchoChar('•');
            setForeground(defaultColor);
        }
    }

    public char[] getPassword() {
        if (defaultColor != getForeground() && super.getText().equals(hint)) {
            return new char[]{};
        }
        else {
            return super.getPassword();
        }
    }
}