package net.glasslauncher.legacy.util;

import net.chris54721.openmcauthenticator.OpenMCAuthenticator;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.components.LoginPanel;
import net.glasslauncher.legacy.jsontemplate.LoginInfo;

import java.awt.*;

public class LoginVerifier {

    public static boolean verifyLogin(boolean canOffline, LoginPanel loginPanel, Window parent) {
        if (Config.getLauncherConfig().isMSToken()) {
            Main.LOGGER.info("Verifying stored MS auth token...");
            if (!(new MSLoginHandler(parent)).verifyStoredToken()) {
                Main.LOGGER.severe("Unable to verify stored MS auth token.");
                Config.getLauncherConfig().setLoginInfo(null);
                return false;
            }
            loginPanel.getUsername().setText(Config.getLauncherConfig().getLoginInfo().getUsername());
            Main.LOGGER.info("MS auth token has been verified!");
        }
        else {
            if (Config.getLauncherConfig().getLoginInfo() != null) {
                Main.LOGGER.info("Verifying stored Mojang auth token...");
                try {
                    OpenMCAuthenticator.validate(Config.getLauncherConfig().getLoginInfo().getAccessToken(), Config.getLauncherConfig().getClientToken());
                    Main.LOGGER.info("Mojang auth token has been verified!");
                    return true;
                } catch (Exception e) {
                    Main.LOGGER.warning("Unable to verify stored Mojang auth token.");
                    Config.getLauncherConfig().setLoginInfo(null);
                    return false;
                }
            }
            String pass = "";
            if (loginPanel.getPassword().getForeground() != Color.gray) {
                pass = String.valueOf(loginPanel.getPassword().getPassword());
            }
            if (!pass.isEmpty()) {
                MojangLoginHandler.login(loginPanel.getUsername().getText(), pass);
                LoginInfo loginInfo = Config.getLauncherConfig().getLoginInfo();
                if (loginInfo == null) {
                    Main.LOGGER.severe("Unable to log in!");
                    return false;
                }
            }
            if (!loginPanel.getUsername().getText().isEmpty() && canOffline) {
                Config.getLauncherConfig().setLoginInfo(new LoginInfo(loginPanel.getUsername().getText(), ""));
            }
            return Config.getLauncherConfig().getLoginInfo() != null;
        }
        return true;
    }
}
