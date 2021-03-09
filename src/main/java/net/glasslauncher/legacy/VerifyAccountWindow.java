package net.glasslauncher.legacy;

import lombok.Getter;
import net.glasslauncher.legacy.components.HintPasswordField;
import net.glasslauncher.legacy.components.HintTextField;
import net.glasslauncher.legacy.components.JButtonScaling;
import net.glasslauncher.legacy.util.MojangLoginHandler;

import javax.swing.*;
import java.awt.*;

public class VerifyAccountWindow extends JDialog {
    @Getter boolean loginValid = false;

    protected JPasswordField password;
    protected HintTextField username;

    public VerifyAccountWindow(Window frame) {
        super(frame);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle("Verify Account");

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(186, 100));

        // Username field
        username = new HintTextField("Username or Email");
        if (Config.getLauncherConfig().getLastUsedEmail() != null) {
            username.setText(Config.getLauncherConfig().getLastUsedEmail());
        }
        username.setBounds(10, 14, 166, 22);
        username.addActionListener((e) -> {
            login();
        });

        // Password field
        password = new HintPasswordField("Password");
        password.setBounds(10, 40, 166, 22);
        password.addActionListener((e) -> {
            login();
        });

        // Login button
        JButtonScaling login = new JButtonScaling();
        login.setText("Login");
        login.setBounds(58, 64, 70, 22);
        login.setOpaque(false);
        login.addActionListener(event -> {
            login();
        });

        panel.add(username);
        panel.add(password);
        panel.add(login);

        add(panel);
        pack();
        setLocationRelativeTo(frame);

        setVisible(true);
    }

    private void login() {
        String pass = "";
        if (password.getForeground() != Color.gray) {
            pass = String.valueOf(password.getPassword());
        }
        if (!pass.isEmpty() && !username.getText().isEmpty()) {
            MojangLoginHandler.login(username.getText(), pass);
            if (Config.getLauncherConfig().getLoginInfo() == null) {
                JOptionPane.showMessageDialog(this, "Invalid username or password.");
                password.setText("");
                return;
            }
            Config.getLauncherConfig().setLastUsedEmail(username.getText());
            Config.getLauncherConfig().saveFile();
            loginValid = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Username or password is empty.");
            password.setText("");
        }
    }
}
