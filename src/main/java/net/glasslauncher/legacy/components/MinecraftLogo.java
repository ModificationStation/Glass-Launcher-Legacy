package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.Main;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MinecraftLogo extends JPanel {
    private Image bgimage;

    public MinecraftLogo() {
        setOpaque(true);
        try {
            BufferedImage logoimg = ImageIO.read(Main.class.getResource("assets/logo.png"));
            int w = logoimg.getWidth();
            int h = logoimg.getHeight();
            bgimage = logoimg.getScaledInstance(w, h, 16);
            setPreferredSize(new Dimension(w + 32, h + 32));
        } catch (IOException e) {
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
