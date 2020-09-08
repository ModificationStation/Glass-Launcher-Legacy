package net.glasslauncher.legacy.components;

import javax.swing.JButton;
import java.awt.Insets;

public class JButtonScaling extends JButton {

    /**
     * Creates a button with no set text or icon.
     */
    public JButtonScaling() {
        setMargin(new Insets(0, 0, 0, 0));
        setOpaque(false);
    }
}
