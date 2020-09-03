package net.glasslauncher.legacy.components;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class JLabelFancy extends JLabel {

    public JLabelFancy(String text) {
        super(text);
        setOpaque(false);
        setBorder(new EmptyBorder(0, 4, 0, 4));
        setForeground(new Color(218, 218, 218));
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.7f));
        g2d.setColor(new Color(0, 0, 0, 178));
        g2d.drawRoundRect(0, 0, width-1, height-1, 4, 4);
        g2d.setColor(new Color(76, 76, 76, 178));
        g2d.fillRoundRect(0, 0, width-1, height-1, 4, 4);
        super.paintComponent(g);
    }
}
