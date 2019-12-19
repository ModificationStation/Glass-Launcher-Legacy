package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

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
