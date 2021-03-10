package net.glasslauncher.legacy;

import com.sun.javafx.application.PlatformImpl;
import net.glasslauncher.common.LoggerFactory;

import javax.swing.*;
import java.util.logging.*;

public class Main {
    public static final Logger LOGGER = LoggerFactory.makeLogger("GlassLauncher", "glass-launcher");

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
        // Bad javafx, no cleanup for you.
        PlatformImpl.setImplicitExit(false);
        // Make tooltips show faster.
        ToolTipManager.sharedInstance().setInitialDelay(0);

        ConsoleWindow console = null;
        if (System.console() != null) {
            LOGGER.info("Detected running with a console.");
        }
        else {
            console = new ConsoleWindow();
            LOGGER.info("Detected not running with a console. Creating a console window...");
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
            else if (arg.equals("-help") || arg.equals("-h")) {
                LOGGER.info(
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
