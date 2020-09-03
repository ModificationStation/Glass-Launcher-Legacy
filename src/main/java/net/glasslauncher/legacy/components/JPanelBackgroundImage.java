package net.glasslauncher.legacy.components;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;

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
        int width = getWidth();
        int height = getHeight();
        for (int x = 0; x < width; x += tileImage.getWidth()) {
            for (int y = 0; y < height; y += tileImage.getHeight()) {
                g.drawImage(tileImage, x, y, this);
            }
        }
    }
}