package net.glass.glassl;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Logo extends JPanel {
    private Image bgimage;

    public Logo() {
        setOpaque(true);
        try {
            BufferedImage logoimg = ImageIO.read(MainWindow.class.getResource("assets/logo.png"));
            int w = logoimg.getWidth();
            int h = logoimg.getHeight();
            bgimage = logoimg.getScaledInstance(w, h, 16);
            setPreferredSize(new Dimension(w+32, h+32));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paintComponent(Graphics g) {
        g.drawImage(this.bgimage, 24, 24, null);
    }

}
