package net.glasslauncher.legacy;

import net.glasslauncher.proxy.Proxy;

import java.io.File;

public class ProxyStandalone {
    /**
     * Main function of the standalone proxy
     * @param args
     */
    public static void main(String[] args) {
        boolean doSound = false;
        boolean doSkin = false;
        boolean doCape = false;
        boolean doLogin = false;
        for (String arg : args) {
            if (arg.toLowerCase().equals("-dosound")) {
                doSound = true;
            } else if (arg.toLowerCase().equals("-doskin")) {
                doSkin = true;
            } else if (arg.toLowerCase().equals("-docape")) {
                doCape = true;
            } else if (arg.toLowerCase().equals("-dologin")) {
                doLogin = true;
            }
        }
        if (!doSound && !doSkin && !doCape && !doLogin) {
            Main.LOGGER.info("No proxy arguments provided! Defaulting to all enabled.");
            doSound = true;
            doSkin = true;
            doCape = true;
        }
        try {
            new File(Config.CACHE_PATH).mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Proxy proxy = new Proxy(new boolean[]{doSound, doSkin, doCape, doLogin});
        proxy.start();
    }
}
