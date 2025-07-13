package net.glasslauncher.legacy;

import net.glasslauncher.proxy.Proxy;

import java.io.File;

public class ProxyStandalone {
    /**
     * Main function of the standalone proxy
     */
    public static void main(String[] args) {
        boolean doSound = false;
        boolean doSkin = false;
        boolean doCape = false;
        boolean doLogin = false;
        for (String arg : args) {
			switch (arg.toLowerCase()) {
				case "-dosound":
					doSound = true;
					break;
				case "-doskin":
					doSkin = true;
					break;
				case "-docape":
					doCape = true;
					break;
				case "-dologin":
					doLogin = true;
					break;
			}
        }
        if (!doSound && !doSkin && !doCape && !doLogin) {
            Main.getLogger().info("No proxy arguments provided! Defaulting to all enabled.");
            doSound = true;
            doSkin = true;
            doCape = true;
        }
        try {
            new File(Config.CACHE_PATH).mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Proxy proxy = new Proxy(new boolean[]{doSound, doSkin, doCape, doLogin}) {
            @Override
            public void run() {
                super.run();
            }
        };
        proxy.start();
    }
}
