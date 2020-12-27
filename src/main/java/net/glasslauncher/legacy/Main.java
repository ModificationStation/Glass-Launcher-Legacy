package net.glasslauncher.legacy;

import lombok.Getter;
import net.glasslauncher.common.CommonConfig;

import javax.swing.UIManager;
import java.util.logging.Logger;

public class Main {
    @Getter private static Logger logger = CommonConfig.makeLogger("GlassLauncher", "glass-launcher");
    public static MainWindow mainwin;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            UIManager.put("TabbedPane.contentOpaque", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ConsoleWindow console = null;
        if (System.console() != null) {
            getLogger().info("Detected running with a console.");
        }
        else {
            console = new ConsoleWindow();
            getLogger().info("Detected not running with a console. Creating a console window...");
        }

        try {
            Config.loadConfigFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String arg : args) {
            if (arg.equals("-proxy")) {
                ProxyStandalone.main(args);
                return;
            }
            else if (arg.equals("-help")) {
                getLogger().info(
                        "\n" +
                                "-proxy      : Launches in proxy only mode. No GUI aside from the console is shown. Defaults to all options enabled unless other paramaters are passed.\n" +
                                " -dosound   : Enables the sound part of the proxy.\n" +
                                " -dologin   : Enables the login part of the proxy. The server you are joining must be using an online fix for this to do anything.\n" +
                                " -doskin    : Enables the skin part of the proxy. Fixes all references to player skins through Mojang/Minecraft servers.\n" +
                                " -docape    : Enables the cape part of the proxy. Fixes all references to player capes through Mojang/Minecraft servers."
                                //"-installdir : Changes install dir to the specified path." !! not implemented !!
                );
                return;
            }
        }
        mainwin = new MainWindow(console);
    }

    /**
     * Checks if the main launcher window is active.
     *
     * @return True if active, False otherwise.
     */
    public static boolean isLauncherActive() {
        return mainwin != null;
    }
}
