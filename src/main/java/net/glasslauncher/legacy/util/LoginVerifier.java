package net.glasslauncher.legacy.util;

import gg.codie.mineonline.gui.MicrosoftLoginController;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.MSLoginWindow;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.jsontemplate.LoginInfo;

import javax.swing.*;
import java.awt.*;

public class LoginVerifier {

    public static boolean verifyLogin(Window parent, boolean canOffline) {
        Main.mainwin.setHasToken(false);

        LoginInfo loginInfo = Config.getLauncherConfig().getLoginInfo();
        if((canOffline && loginInfo != null && loginInfo.getUsername() != null && !loginInfo.getUsername().isEmpty() && loginInfo.getAccessToken() == null)) {
            Main.LOGGER.info("Got given a name, but a null token. Working in offline mode.");
            Main.mainwin.setHasToken(true);
            return true;
        }

        new MSLoginWindow(parent);

        if(MicrosoftLoginController.getError() == null && Config.getLauncherConfig().getLoginInfo() != null && Config.getLauncherConfig().getLoginInfo().getAccessToken() != null) {
            Main.LOGGER.info("Successfully validated login.");
            Main.mainwin.setHasToken(true);
            return true;
        }

        while(true) {
            if(canOffline) {
                int response = JOptionPane.showConfirmDialog(parent, "Unable to login! Do you want to continue in offline mode?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    String name = null;
                    LoginInfo loginInfo2 = Config.getLauncherConfig().getLoginInfo();
                    while(name == null || name.isEmpty()) {
                        name = JOptionPane.showInputDialog(parent, "Enter the username you want to use: ", loginInfo2 != null ? loginInfo2.getUsername() : null);
                    }
                    Config.getLauncherConfig().setLoginInfo(new LoginInfo(name, null, null));
                    Main.mainwin.setUsername(name);
                    Main.LOGGER.info("Skipping login and proceeding in offline mode.");
                    Main.mainwin.setHasToken(true);
                    return true;
                }
                else if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION) {
                    Main.LOGGER.info("Aborted login.");
                    return false;
                }
            }
            else {
                Main.LOGGER.severe("Logging in is required.");
                int response = JOptionPane.showConfirmDialog(parent, "Unable to login! Do you want to try again?", "Warning", JOptionPane.YES_NO_OPTION);
                if(response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
                    Main.LOGGER.info("Aborted login.");
                    return false;
                }
            }

            new MSLoginWindow(parent);

            if(MicrosoftLoginController.getError() == null && Config.getLauncherConfig().getLoginInfo() != null && Config.getLauncherConfig().getLoginInfo().getAccessToken() != null) {
                Main.LOGGER.info("Logged in successfully.");
                Main.mainwin.setHasToken(true);
                return true;
            }
        }
    }
}
