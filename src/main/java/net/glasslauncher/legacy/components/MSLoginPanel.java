package net.glasslauncher.legacy.components;

import gg.codie.mineonline.gui.MicrosoftLoginController;
import lombok.Getter;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.components.templates.JButtonScaling;

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MSLoginPanel extends JPanel {

    @Getter private TextField usernameField;

    @Getter private JButtonScaling loginButton;
    private JButton logoutButton;

    public MSLoginPanel(ActionListener msListener) {
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(279, 100));
        makeGUI(msListener);
    }

    private void makeGUI(ActionListener msListener) {
        // Token field
        usernameField = new TextField(MicrosoftLoginController.getLoginCode());
        if (Config.getLauncherConfig().getLastUsedUsername() != null) {
            usernameField.setText(Config.getLauncherConfig().getLastUsedUsername());
        }
        usernameField.setBounds(24, 14, 166, 22);
        usernameField.addActionListener(msListener);


        // This is fake.
        HintPasswordField passwordField = new HintPasswordField("Password");
        passwordField.setBounds(24, 40, 166, 22);
        passwordField.setText("oh boy, pass");
        passwordField.setEnabled(false);

        // Login button
        loginButton = new JButtonScaling();
        loginButton.setText("Login");
        loginButton.setBounds(192, 40, 70, 22);
        loginButton.setOpaque(false);
        loginButton.addActionListener(msListener);

        try {
            logoutButton = new JButton(new ImageIcon(ImageIO.read(Objects.requireNonNull(Main.class.getResourceAsStream("assets/logout.png")))));
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
        add(usernameField);
        add(loginButton);
        add(passwordField);
    }

    public void setHasToken(boolean hasToken) {
        logoutButton.setEnabled(hasToken);
        usernameField.setEnabled(!hasToken);
        loginButton.setText(hasToken? "Continue" : "Login");
    }
}
