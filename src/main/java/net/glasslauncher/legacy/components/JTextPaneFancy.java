package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.Config;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class JTextPaneFancy extends JTextPane {


    public JTextPaneFancy() {
        super();
        if (!Config.getLauncherConfig().isThemeDisabled()) {
            setOpaque(false);
            setBorder(new EmptyBorder(0, 4, 0, 4));
            setForeground(new Color(218, 218, 218));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!Config.getLauncherConfig().isThemeDisabled()) {
            int width = getWidth();
            int height = getHeight();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.7f));
            g2d.setColor(new Color(0, 0, 0, 178));
            g2d.drawRoundRect(0, 0, width - 1, height - 1, 4, 4);
            g2d.setColor(new Color(76, 76, 76, 178));
            g2d.fillRoundRect(0, 0, width - 1, height - 1, 4, 4);
        }
        super.paintComponent(g);
    }
}
