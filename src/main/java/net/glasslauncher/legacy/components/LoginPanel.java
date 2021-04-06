package net.glasslauncher.legacy.components;

import lombok.Getter;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.components.templates.JButtonScaling;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {

    @Getter private HintTextField username;
    @Getter private HintPasswordField password;
    private JButtonScaling login;
    private JButton msAuthButton;
    private JButton logoutButton;

    public LoginPanel(ActionListener usernameListener, ActionListener msListener) {
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(279, 100));
        makeGUI(usernameListener, msListener);
    }

    private void makeGUI(ActionListener mojangListener, ActionListener msListener) {
        // Username field
        username = new HintTextField("Username or Email");
        if (Config.getLauncherConfig().getLastUsedEmail() != null) {
            username.setText(Config.getLauncherConfig().getLastUsedEmail());
        }
        username.setBounds(24, 14, 166, 22);
        username.addActionListener(mojangListener);

        // Password field
        password = new HintPasswordField("Password");
        password.setBounds(24, 40, 166, 22);
        password.addActionListener(mojangListener);

        // Login button
        login = new JButtonScaling();
        login.setText("Login");
        login.setBounds(192, 40, 70, 22);
        login.setOpaque(false);
        login.addActionListener(mojangListener);

        // MS Auth
        if (!Config.getLauncherConfig().isHidingMSButton()) {
            try {
                msAuthButton = new JButton(new ImageIcon(ImageIO.read(Main.class.getResourceAsStream("assets/ms.png"))));
                msAuthButton.setBorder(BorderFactory.createEmptyBorder());
                msAuthButton.setContentAreaFilled(false);
                msAuthButton.setBounds(0, 40, 22, 22);
                msAuthButton.addActionListener(msListener);

            } catch (Exception e) {
                Main.LOGGER.severe("This exception should not be possible!");
                e.printStackTrace();
            }
            add(msAuthButton);
        }
        try {
            logoutButton = new JButton(new ImageIcon(ImageIO.read(Main.class.getResourceAsStream("assets/logout.png"))));
            logoutButton.setBorder(BorderFactory.createEmptyBorder());
            logoutButton.setContentAreaFilled(false);
            logoutButton.setBounds(0, 66, 22, 22);
            logoutButton.addActionListener((e) -> {
                Config.getLauncherConfig().setLoginInfo(null);
                setHasToken(false);
            });
            add(logoutButton);
        } catch (Exception e) {
            Main.LOGGER.severe("This exception should not be possible!");
            e.printStackTrace();
        }
        add(username);
        add(password);
        add(login);
    }

    public void setHasToken(boolean hasToken) {
        logoutButton.setEnabled(hasToken);
        if (msAuthButton != null) {
            msAuthButton.setEnabled(!hasToken);
        }
        username.setEnabled(!hasToken);
        password.setEnabled(!hasToken);
        login.setText(hasToken? "Continue" : "Login");
    }
}
