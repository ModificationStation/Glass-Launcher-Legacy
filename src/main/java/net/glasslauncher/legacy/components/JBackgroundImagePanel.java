package net.glasslauncher.legacy.components;

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;

public class JBackgroundImagePanel extends JPanel {
    private BufferedImage tileImage;

    public JBackgroundImagePanel(URL imagePath) {
        try {
            tileImage = ImageIO.read(imagePath.openStream());
        } catch (Exception e) {
            e.printStackTrace();
            tileImage = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
        }
    }


    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        for (int x = 0; x < width; x += tileImage.getWidth()) {
            for (int y = 0; y < height; y += tileImage.getHeight()) {
                g.drawImage(tileImage, x, y, this);
            }
        }
    }
}
