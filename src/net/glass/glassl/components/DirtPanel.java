package net.glass.glassl.components;

import net.glass.glassl.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DirtPanel extends JPanel {
    private Image bgImage;
    private Image img;

    public DirtPanel() {
        setOpaque(true);

        try {
            this.bgImage = ImageIO.read(Main.class.getResource("assets/background.png")).getScaledInstance(32, 32, 16);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }


    public void update(Graphics g) {
        paint(g);
    }

    public void paintComponent(Graphics paramGraphics) {
        int w = getWidth() / 2 + 1;
        int h = getHeight() / 2 + 1;
        if (this.img == null || this.img.getWidth(null) != w || this.img.getHeight(null) != h) {
            this.img = createImage(w, h);

            Graphics graphics = this.img.getGraphics();
            for (byte b = 0; b <= w / 32; b++) {
                for (byte b1 = 0; b1 <= h / 32; b1++)
                    graphics.drawImage(this.bgImage, b * 32, b1 * 32, null);
            }
            if (graphics instanceof Graphics2D) {
                Graphics2D graphics2D = (Graphics2D) graphics;
                int k = 1;
                graphics2D.setPaint(new GradientPaint(new Point2D.Float(0.0F, 0.0F), new Color(553648127, true), new Point2D.Float(0.0F, k), new Color(0, true)));
                graphics2D.fillRect(0, 0, w, k);

                k = h;
                graphics2D.setPaint(new GradientPaint(new Point2D.Float(0.0F, 0.0F), new Color(0, true), new Point2D.Float(0.0F, k), new Color(1610612736, true)));
                graphics2D.fillRect(0, 0, w, k);
            }
            graphics.dispose();
        }
        paramGraphics.drawImage(this.img, 0, 0, w * 2, h * 2, null);
    }
}