package net.glasslauncher.legacy;

import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Main {
    public static Logger LOGGER;

    public static MainWindow mainwin;

    public static void main(String[] args) {
        if (!System.getProperty("java.version").startsWith("1.8")) {
            JOptionPane.showMessageDialog(null, "Unsupported java version found!\n" + System.getProperty("java.version") + " was found, but java 1.8 is the only version supported.");
        }
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
        // Make tooltips show faster.
        ToolTipManager.sharedInstance().setInitialDelay(500);

        List<String> listArgs = Arrays.asList(args);
        if (listArgs.contains("-installdir")) {
            try {
                String instPath = listArgs.get(listArgs.indexOf("-installdir") + 1);
                if (instPath.startsWith("-")) {
                    System.err.println("You must provide a path in the argument slot after -installdir, not another argument!");
                }
                File instFile = new File(instPath);
                if (!instFile.exists()) {
                    System.out.println("\"" + instFile.getAbsolutePath() + "\" does not exist.");
                    int response = JOptionPane.showConfirmDialog(null, "\"" + instFile.getAbsolutePath() + "\" does not exist.\nAre you sure you want to continue?");
                    if (response != JOptionPane.YES_OPTION) {
                        System.out.println("Aborting launch.");
                        return;
                    }
                    System.out.println("Creating directories.");
                    instFile.mkdirs();
                }
                else if (!(new File(instFile, "launcher_config.json")).exists()) {
                    System.out.println("\"" + instFile.getAbsolutePath() + "\" does not exist.");
                    int response = JOptionPane.showConfirmDialog(null, "\"" + instFile.getAbsolutePath() + "\" exists, but does not contain a valid launcher config.\nAre you sure you want to continue?");
                    if (response != JOptionPane.YES_OPTION) {
                        System.out.println("Aborting launch.");
                        return;
                    }
                }
                instPath = instFile.getAbsolutePath();
                if (!instPath.endsWith("/")) {
                    instPath += "/";
                }
                CommonConfig.setOverridePath(instPath);
            } catch (Exception e) {
                System.err.println("Failed to parse installdir parameter!");
                throw new RuntimeException(e);
            }
        }

        LOGGER = LoggerFactory.makeLogger("GlassLauncher", "glass-launcher");

        ConsoleWindow console = null;
        if (System.console() != null || listArgs.contains("-noguiconsole")) {
            LOGGER.info("Detected running with a console.");
        }
        else {
            LOGGER.info("Detected not running with a console. Creating a console window...");
            console = new ConsoleWindow();
        }

        try {
            Config.loadConfigFiles();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Something went extremely wrong reading config!");
        }

        // Bad javafx, no cleanup for you cause you like to break.
        try {
            Class.forName("com.sun.javafx.application.PlatformImpl");
            com.sun.javafx.application.PlatformImpl.setImplicitExit(false);
        } catch (Exception e) {
            Main.LOGGER.warning("Can't find a valid JavaFX installation! Disabling Microsoft authentication.");
            Config.getLauncherConfig().setHidingMSButton(true);
        }

        for (String arg : args) {
            if (arg.equals("-help") || arg.equals("-h")) {
                LOGGER.info(
                        "\n" +
                                "-noguiconsole  : Forces the launcher to run without a console window.\n" +
                                "-installdir    : Changes install dir to the specified path."
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
