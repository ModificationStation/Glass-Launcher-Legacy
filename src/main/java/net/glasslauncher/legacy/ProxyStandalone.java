package net.glasslauncher.legacy;

import net.glasslauncher.legacy.mc.Proxy;

public class ProxyStandalone {
    /**
     * Main function of the standalone proxy
     * @param args
     */
    public static void main(String[] args) {
        boolean doSound = false;
        boolean doSkin = false;
        boolean doCape = false;
        for (String arg : args) {
            if (arg.toLowerCase().equals("-dosound")) {
                doSound = true;
            } else if (arg.toLowerCase().equals("-doskin")) {
                doSkin = true;
            } else if (arg.toLowerCase().equals("-docape")) {
                doCape = true;
            }
        }
        if (!doSound && !doSkin && !doCape) {
            Main.logger.info("No proxy arguments provided! Defaulting to all enabled.");
            doSound = true;
            doSkin = true;
            doCape = true;
        }
        Proxy proxy = new Proxy(new boolean[]{doSound, doSkin, doCape});
        proxy.start();
    }
}
