package net.glasslauncher.legacy.components;

import javax.swing.border.EmptyBorder;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class JTextFieldFancy extends HintTextField {

    public JTextFieldFancy(String hint) {
        super(hint, new Color(218, 218, 218), Color.GRAY);
        //setBorder(new EmptyBorder(0, 4, 0, 4));
        setBorder(new EmptyBorder(0, 4, 0, 4));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        g2d.setColor(new Color(22, 13, 16, 153));
        g2d.drawRoundRect(0, 0, width-1, height-1, 4, 4);
        g2d.setColor(new Color(37, 30, 24, 255));
        g2d.fillRoundRect(1, 1, width-2, height-2, 4, 4);
        super.paintComponent(g);
    }
}
