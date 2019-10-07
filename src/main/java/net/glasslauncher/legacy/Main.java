package net.glasslauncher.legacy;

import net.glasslauncher.legacy.util.Classpath;
import net.glasslauncher.legacy.util.FileUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.*;

public class Main {
    public static Logger logger = Logger.getLogger("launcher");

    private static ArrayList libs = new ArrayList();
    private static MainWindow mainwin = null;

    private static void makeLogger() {
        try {
            System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT] [GlassL] [%4$s] %5$s %n");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();
            String time = dtf.format(now);
            File logdir = new File(Config.glasspath + "/glass-logs/launcher");
            logdir.mkdirs();
            Handler file_handler = new FileHandler(Config.glasspath + "/glass-logs/launcher/" + time + ".log");
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
        Config.easyMineLauncherFile = (String) libs.get(0);

        for (Object lib : libs.toArray()) {
            try {
                Classpath.addFile(Config.glasspath + "lib/" + lib);
            } catch (Exception e) {
                logger.info("Failed to load \"" + lib + "\".");
                e.printStackTrace();
            }
        }

        for (String arg : args) {
            if (arg.equals("-proxy")) {
                ProxyStandalone.main(args);
                return;
            }
        }

        mainwin = new MainWindow(args, console);
    }

    /**
     * Downloads all dependencies listed in dependencies.gradle.
     * Format for downloadable dependency is: " {4}//[url],[md5]".
     *
     * @return True if any were downloaded, False if none were downloaded.
     */
    public static void getDeps() {
        logger.info("Checking dependencies...");
        String file;
        try {
            file = new Scanner(MainWindow.class.getResourceAsStream("/dependencies.gradle"), "UTF-8").useDelimiter("\\A").next();
        } catch (Exception e) {
            logger.info("Failed to get dependencies from dependencies.gradle.");
            e.printStackTrace();
            return;
        }
        for (String line : file.split("\n")) {
            if (line.startsWith("    // ")) {
                String[] parts = line.replaceFirst(" {4}// ", "").split(",");
                try {
                    libs.add(parts[0].substring(parts[0].lastIndexOf('/') + 1));
                    FileUtils.downloadFile(parts[0], Config.glasspath + "lib/" + "", parts[1].replace("\r", "").replace("\n", ""));
                } catch (Exception e) {
                    logger.info("Failed to download dependency. Invalid formatting?");
                    e.printStackTrace();
                }
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
