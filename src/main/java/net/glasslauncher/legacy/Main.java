package net.glasslauncher.legacy;

import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.LoggerFactory;
import net.glasslauncher.legacy.jsontemplate.PreLaunchDep;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import static net.glasslauncher.legacy.Config.OS;

public class Main {
    public static Logger LOGGER;

    public static MainWindow mainwin;

    public static void main(String[] args) throws URISyntaxException, IOException {
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

        String glassPath = listArgs.contains("-installdir")? listArgs.get(listArgs.indexOf("-installdir") + 1) : (OS.equals("windows") ?
                System.getenv("AppData").replaceAll("\\\\", "/") + "/.glass-launcher/"
                :
                OS.equals("osx") ? System.getProperty("user.home") + "/Library/Application Support/.glass-launcher/" : System.getProperty("user.home") + "/.glass-launcher/");

        loadDeps("/dependencies.gmc", glassPath);

        boolean hasCommons = false;
        try {
            net.glasslauncher.common.CommonConfig.getGlassPath();
            hasCommons = true;
            System.out.println("====Found jar!====");
        } catch (Error ignored) {}
        System.err.println(new File(glassPath, "cache/prelaunch/*").getAbsolutePath());

        if (!hasCommons) {
            if(Arrays.asList(args).contains("-secondlaunch")) {
                System.err.println(System.getProperty("java.class.path"));
                System.err.println("Second launch didn't find the cached jars to use!");
                System.exit(0);
            }
            String javaPath = System.getProperty("java.home") + (OS.equals("windows") ? "/bin/java.exe" : "/bin/java");
            File currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // If we are in an IDE/gradle environment, let's not make debugging CBT.
            if(currentJar.getName().endsWith(".jar")) {
                ArrayList<String> newArgs = new ArrayList<>();
                newArgs.add(javaPath);
                newArgs.add("-cp");
                String classpathSep = OS.equals("windows") ? ";" : ":";
                newArgs.add(new File(glassPath + "cache/prelaunch/*").getAbsolutePath().replace("\\", "/") + classpathSep + Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                newArgs.add(Main.class.getCanonicalName());
                newArgs.add("-secondlaunch");
                ProcessBuilder processBuilder = new ProcessBuilder(newArgs);
//                processBuilder.redirectError(new File("processerr.txt"));
//                processBuilder.redirectOutput(new File("processout.txt"));
                processBuilder.start();

                System.out.println("If you're launching from a console, and wish to not have this program reboot to fix itself, pass \"-cp " + newArgs.get(2) + "\" as an argument.");
                System.exit(0);
            }
        }

        if (listArgs.contains("-installdir")) {
            try {
                String instPath = listArgs.get(listArgs.indexOf("-installdir") + 1);
                if (instPath.startsWith("-")) {
                    System.err.println("You must provide a path after -installdir!");
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
        if ((System.console() != null && listArgs.contains("-forceguiconsole")) || listArgs.contains("-noguiconsole")) {
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

        LOGGER.info("Using Java " + System.getProperty("java.version"));
        if (!System.getProperty("java.version").startsWith("1.8")) {
            LOGGER.info("Newer Java version found! " + System.getProperty("java.version") + " was found, but Java 1.8 is what's required for most jar edit mods, including modloader.");
            if(!Config.getLauncherConfig().isHidingJavaWarning()) {
                JOptionPane.showMessageDialog(null, "Newer Java version found!\n" + System.getProperty("java.version") + " was found, but Java 1.8 is what's required for most jar edit mods, including modloader.\nYou can disable this pop-up in the instances tab.");
            }
        }

        for (String arg : args) {
            if (arg.equals("-help") || arg.equals("-h")) {
                LOGGER.info(
                        "\n" +
                                "-noguiconsole  : Forces the launcher to run without a console window.\n" +
                                "-forceguiconsole  : Forces the launcher to run with a console window.\n" +
                                "-installdir    : Changes install dir to the specified path."
                );
                return;
            }
        }

        mainwin = new MainWindow(console);
    }

    /**
     * Downloads all dependencies listed in the provided json file. Only uses files inside the classpath.
     */
    private static void loadDeps(String jsonPath, String glassPath) {
        List<String> libs = new ArrayList<>();
        //noinspection ConstantConditions If this nullpointers, we have bigger issues.
        if(!Main.class.getClassLoader().getResource("net/glasslauncher/legacy/Main.class").getProtocol().equals("jar")) {
            System.out.println("Detected running from IDE/Gradle. Skipping dependency sideloading steps.");
            return;
        }
        System.out.println("Checking dependencies...");

        InputStream depsJson = Main.class.getResourceAsStream(jsonPath);
        if(depsJson == null) {
            return;
        }
        Scanner s = (new Scanner(depsJson)).useDelimiter("\\A");
        String depsString = s.hasNext() ? s.next() : "";
        for (String entry : depsString.split("\\|\\|")) {
            String[] values = entry.split("\\|");
            PreLaunchDep dep = new PreLaunchDep(values[0], values[1], glassPath + "/cache/");
            System.out.println("Validating " + dep);
            dep.jsonPostProcess();
            dep.cache();
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
