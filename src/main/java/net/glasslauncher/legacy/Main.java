package net.glasslauncher.legacy;

import lombok.Getter;
import net.glasslauncher.legacy.util.Classpath;
import net.glasslauncher.legacy.util.FileUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.*;

public class Main {
    @Getter private static Logger logger = Logger.getLogger("launcher");
    private static ArrayList<String> libs = new ArrayList<>();
    private static MainWindow mainwin = null;

    private static void makeLogger() {
        try {
            System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT] [GlassL] [%4$s] %5$s %n");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();
            String time = dtf.format(now);
            File logdir = new File(Config.getGLASS_PATH() + "/glass-logs/launcher");
            logdir.mkdirs();
            Handler file_handler = new FileHandler(Config.getGLASS_PATH()+ "/glass-logs/launcher/" + time + ".log");
            SimpleFormatter format = new SimpleFormatter();
            logger.addHandler(file_handler);
            file_handler.setFormatter(format);
            logger.setLevel(Level.ALL);
            file_handler.setLevel(Level.ALL);
            logger.info("Logging to " + logdir.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ConsoleWindow console = new ConsoleWindow();
        makeLogger();

        getDeps();
        Config.setEasyMineLauncherFile(libs.get(0));

        for (Object lib : libs.toArray()) {
            try {
                Classpath.addFile(Config.getGLASS_PATH() + "lib/" + lib);
            } catch (Exception e) {
                logger.info("Failed to load \"" + lib + "\".");
                e.printStackTrace();
            }
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
        }
        mainwin = new MainWindow(console);
    }

    /**
     * Downloads all dependencies listed in Config.Deps.cactusDeps.
     */
    private static void getDeps() {
        getLogger().info("Checking dependencies...");

        for (String dep : Config.getGLASS_DEPS().keySet()) {
            try {
                FileUtils.downloadFile(dep, Config.getGLASS_PATH() + "/lib/", Config.getGLASS_DEPS().get(dep));
                libs.add(dep.substring(dep.lastIndexOf('/') + 1));
            } catch (Exception e) {
                getLogger().info("Failed to download dependency. Invalid formatting?");
                e.printStackTrace();
            }
        }
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
