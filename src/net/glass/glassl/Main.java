package net.glass.glassl;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.*;

public class Main {
    static Logger logger = Logger.getLogger("launcher");

    private static void makeLogger() {
        try {
            System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT] [GlassL] [%4$s] %5$s %n");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();
            String time = dtf.format(now);
            File logdir = new File(Config.instpath + "/glass-logs/launcher");
            logdir.mkdirs();
            Handler file_handler = new FileHandler(Config.instpath + "/glass-logs/launcher/" + time + ".log");
            SimpleFormatter format = new SimpleFormatter();
            logger.addHandler(file_handler);
            file_handler.setFormatter(format);
            logger.setLevel(Level.ALL);
            file_handler.setLevel(Level.ALL);
            logger.info("Logging to " + logdir.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        long totalmem = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        AWTConsole console = new AWTConsole();
        Frame consoleframe = console.getConsoleFrame();
        makeLogger();

        logger.info("RAM Allocation: " + totalmem + "MB");


        if (totalmem < 300) {
            boolean restartwithmoreram;
            restartwithmoreram = 0 == JOptionPane.showConfirmDialog(null, "Your RAM allocation is low. Restart with 1024M RAM?", "alert", JOptionPane.YES_NO_OPTION);
            if (restartwithmoreram) {
                restartWithMoreRAM();
            }

            logger.warning("Your RAM allocation is low.");
            logger.warning("Restart the program with -Xmx1024m added to the java arguments.");
            logger.warning("That will prevent minecraft from running out of memory.");
        }

        MainWindow mainwin = new MainWindow(args, consoleframe);
    }

    private static void restartWithMoreRAM() {
        logger.info("Restarting with more RAM! (1024M)");
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar;
        try {
            currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        if(!currentJar.getName().endsWith(".jar"))
            return;

        final ArrayList<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-Xmx1024M");
        command.add("-jar");
        command.add(currentJar.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.exit(0);
    }
}
