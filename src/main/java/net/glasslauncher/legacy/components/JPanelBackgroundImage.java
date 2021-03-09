package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.Config;

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;

public class JPanelBackgroundImage extends JPanel {
    private BufferedImage tileImage;

    public JPanelBackgroundImage(URL imageFile) {
        super();
        try {
            tileImage = ImageIO.read(imageFile.openStream());
        } catch (Exception e) {
            e.printStackTrace();
            tileImage = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!Config.getLauncherConfig().isThemeDisabled()) {
            int width = getWidth();
            int height = getHeight();
            for (int x = 0; x < width; x += tileImage.getWidth()) {
                for (int y = 0; y < height; y += tileImage.getHeight()) {
                    g.drawImage(tileImage, x, y, this);
                }
            }
        }
        else {
            super.paintComponent(g);
        }
    }
}
