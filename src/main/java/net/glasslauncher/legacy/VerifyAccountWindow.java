package net.glasslauncher.legacy;

import lombok.Getter;
import net.glasslauncher.legacy.components.HintPasswordField;
import net.glasslauncher.legacy.components.HintTextField;
import net.glasslauncher.legacy.components.LoginPanel;
import net.glasslauncher.legacy.components.templates.JButtonScaling;
import net.glasslauncher.legacy.mc.Wrapper;
import net.glasslauncher.legacy.util.LoginVerifier;
import net.glasslauncher.legacy.util.MSLoginHandler;
import net.glasslauncher.legacy.util.MojangLoginHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class VerifyAccountWindow extends JDialog {
    @Getter boolean loginValid = false;

    protected JPasswordField password;
    protected HintTextField username;
    protected LoginPanel loginPanel;

    public VerifyAccountWindow(Window frame) {
        super(frame);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle("Verify Account");

        ActionListener mojangListener = (e) -> {
            loginPanel.setHasToken(true);
            if (!LoginVerifier.verifyLogin(false, loginPanel, this)) {
                loginPanel.setHasToken(false);
                Main.LOGGER.severe("Invalid login!");
                return;
            }
            Config.getLauncherConfig().setLastUsedEmail(loginPanel.getUsername().getText());
            Config.getLauncherConfig().saveFile();
            if (Config.getLauncherConfig().getLoginInfo().getAccessToken().isEmpty()) {
                loginPanel.setHasToken(false);
                Config.getLauncherConfig().setLoginInfo(null);
            }
            loginValid = true;
            dispose();
        };

        ActionListener msListener = (e) -> {
            (new MSLoginHandler(Main.mainwin)).login();
            if (Config.getLauncherConfig().getLoginInfo() != null) {
                loginPanel.getUsername().setText(Config.getLauncherConfig().getLoginInfo().getUsername());
                loginPanel.setHasToken(true);
                loginValid = true;
                dispose();
            }
        };

        loginPanel = new LoginPanel(mojangListener, msListener);
        loginPanel.setHasToken(LoginVerifier.verifyLogin(false, loginPanel, this));

        add(loginPanel);
        pack();
        setLocationRelativeTo(frame);

        setVisible(true);
    }
}
