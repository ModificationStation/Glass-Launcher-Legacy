package net.glasslauncher.legacy.util;

import net.glasslauncher.legacy.Main;

import java.awt.*;
import java.net.*;

public class LinkRedirector {

    public static void openLinkInSystemBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                URI uri = new URI(url);
                Desktop.getDesktop().browse(uri);
            } catch (Throwable e) {
                Main.LOGGER.severe("Error on opening link \"" + url + "\" in system browser.");
                e.printStackTrace();
            }
        } else {
            Main.LOGGER.warning("OS does not support desktop operations like browsing. Cannot open link \"" + url + "\".");
        }
    }
}