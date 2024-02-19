package net.glasslauncher.legacy.mc;

import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.JsonConfig;
import net.glasslauncher.common.LoggerFactory;
import net.glasslauncher.common.PublicPatternFileHandler;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.jsontemplate.InstanceConfig;
import net.glasslauncher.legacy.jsontemplate.MavenDep;
import net.glasslauncher.wrapper.LegacyWrapper;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

public class Launcher {
    private final String instance;

    private final ArrayList<String> args;
    private InstanceConfig instJson;

    public Launcher() {
        this.instance = Config.getLauncherConfig().getLastUsedInstance();
        String instPath = CommonConfig.getGlassPath() + "instances/" + instance + "/.minecraft";

        this.getConfig();

        StringBuilder extraCP = new StringBuilder();

        MavenDep dep = new MavenDep("commons-cli:commons-cli:1.5.0", "https://repo.maven.apache.org/maven2");
        dep.jsonPostProcess();
        dep.cache();
        extraCP.append(";").append(dep.provide());

        dep = new MavenDep("org.json:json:20211205", "https://repo.maven.apache.org/maven2");
        dep.jsonPostProcess();
        dep.cache();
        extraCP.append(";").append(dep.provide());

        dep = new MavenDep("org.apache.commons:commons-text:1.9", "https://repo.maven.apache.org/maven2");
        dep.jsonPostProcess();
        dep.cache();
        extraCP.append(";").append(dep.provide());

        dep = new MavenDep("net.glasslauncher:commons:1.3", "https://maven.glass-launcher.net/snapshots");
        dep.jsonPostProcess();
        dep.cache();
        extraCP.append(";").append(dep.provide());

        dep = new MavenDep("org.apache.commons:commons-lang3:3.12.0", "https://repo.maven.apache.org/maven2");
        dep.jsonPostProcess();
        dep.cache();
        extraCP.append(";").append(dep.provide());

        dep = new MavenDep("com.github.calmilamsy:glass-launch-wrapper:f91c92f", "https://jitpack.io");
        dep.jsonPostProcess();
        dep.cache();
        extraCP.append(";").append(dep.provide());
        if (new File(Config.CACHE_PATH + "intermediary_mappings/" + instJson.getVersion() + ".jar").exists()) {
            Main.LOGGER.info("Adding intermediary mappings for " + instJson.getVersion() + " to classpath.");
            extraCP.append(";").append(Config.CACHE_PATH).append("intermediary_mappings/").append(instJson.getVersion()).append(".jar");
        }
        for (MavenDep mavenDep : instJson.getMavenDeps()) {
            mavenDep.cache();
            extraCP.append(";").append(mavenDep.provide());
        }

        this.args = new ArrayList<>();
        args.add(Config.JAVA_BIN);
        args.add("-Dglasslauncher.wrapper=" + String.join(",", String.valueOf(instJson.isProxySound()), String.valueOf(instJson.isProxySkin()), String.valueOf(instJson.isProxyCape()), String.valueOf(instJson.isProxyLogin()), String.valueOf(instJson.isProxyPiracyCheck())));
        String javaArgs = instJson.getJavaArgs();
        // TODO: Use an actual args parser
        boolean trip = false;
        if (!javaArgs.isEmpty()) {
            for (String arg : javaArgs.split(" -")) {
                if (trip) {
                    args.add("-" + arg);
                } else {
                    args.add(arg);
                    trip = true;
                }
            }
        }
        args.add("-Xmx" + instJson.getMaxRam());
        args.add("-Xms" + instJson.getMinRam());
        args.add("-Djava.library.path=" + instPath + "/bin/natives");
        args.add("-Dfabric.gameJarPath=" + instPath + "/bin/" + instJson.getVersion() + ".jar");
        args.add("-classpath");
        args.add(Config.getAbsolutePathForCP(instance, new String[] {
                ".minecraft/bin/" + instJson.getVersion() + ".jar",
                ".minecraft/bin/lwjgl.jar",
                ".minecraft/bin/lwjgl_util.jar",
                ".minecraft/bin/jinput.jar",
        }) + extraCP);
        System.out.println(args.get(args.size() - 1));
        args.add(LegacyWrapper.class.getCanonicalName());
        args.add("--username");
        args.add(Config.getLauncherConfig().getLoginInfo().getUsername());
        String session = Config.getLauncherConfig().getLoginInfo().getAccessToken();
        if(session != null && !session.isEmpty()) {
            args.add("--session");
            args.add(Config.getLauncherConfig().getLoginInfo().getAccessToken());
        }
        String uuid = Config.getLauncherConfig().getLoginInfo().getUuid();
        if(uuid != null && !uuid.isEmpty()) {
            args.add("--uuid");
            args.add(Config.getLauncherConfig().getLoginInfo().getUuid());
        }
        args.add("--mainClass");
        args.add(instJson.getMainClass());
        if ((instJson.getCustomMinecraftArgs() != null) && !instJson.getCustomMinecraftArgs().isEmpty()) {
            args.add("--modArg");
            args.add(instJson.getCustomMinecraftArgs());
        }
    }

    private void getConfig() {
        String instPath = CommonConfig.getGlassPath() + "instances/" + instance;
        File confFile = new File(instPath + "/instance_config.json");

        if (!confFile.exists()) {
            Main.LOGGER.info("Config file does not exist! Using defaults.");
            instJson = new InstanceConfig(instPath + "/instance_config.json");
        } else {
            try {
                instJson = (InstanceConfig) JsonConfig.loadConfig(confFile.getPath(), InstanceConfig.class);
            } catch (Exception e) {
                Main.LOGGER.info("Config file cannot be read! Using defaults.");
                instJson = new InstanceConfig(instPath + "/instance_config.json");
                e.printStackTrace();
            }
        }
    }

    public void startMC() {
        try {
            // A couple of basic sanity checks for Java.
            Main.LOGGER.info("Using Java install at \"" + instJson.getCustomJava() + "\"");

            Process process = Runtime.getRuntime().exec(new String[]{instJson.getCustomJava(), "-version"});
            // Why the fuck do you print to stderr for this, Java?
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String s;
            boolean is64 = false;
            boolean hasPacking = false;
            while ((s = stdError.readLine()) != null) {
                if (s.toLowerCase().contains("version")) {
                    Main.LOGGER.info(s.split("\"")[1].split("\\.")[0]);
                    if(Integer.parseInt(s.split("\"")[1].split("\\.")[0]) > 8) {
                        hasPacking = true;
                    }
                }
                if (s.contains("64-Bit")) {
                    is64 = true;
                }
            }
            if (hasPacking) {
                Main.LOGGER.warning("Detected using Java newer than 8, forcedisabling launch-wrapper functions.");
                args.add("--disableFixes");
                args.add("true");
                if(!instJson.isHidingJavaWarnings()) {
                    JOptionPane.showMessageDialog(Main.mainwin, "Detected using Java newer than 8, be aware that the built in fixes for minecraft have been disabled for compatibility reasons.");
                }
            }
            if (!is64) {
                String ramToUse = instJson.getMaxRam();
                String ramStep = ramToUse.substring(ramToUse.length() - 1).toLowerCase();
                ramToUse = ramToUse.substring(0, ramToUse.length() - 1);
                int ramInt = Integer.parseInt(ramToUse);
                if ((ramStep.equals("m") && ramInt > 1024) || (ramStep.equals("g") && ramInt > 1)) {
                    Main.LOGGER.warning("Detected using x32 Java! Aborting launch, due to attempting to use more than 1GB of RAM!");
                    JOptionPane.showMessageDialog(Main.mainwin, "Detected using x32 Java! Aborting launch, due to attempting to use more than 1GB of RAM!");
                    return;
                }
                Main.LOGGER.info("Detected using x32 Java. If you're planning on using more than 1GB of RAM for minecraft, you should use the x64 version of Java!");
                if (!instJson.isHidingJavaWarnings()) {
                    JOptionPane.showMessageDialog(Main.mainwin, "Detected using x32 Java. If you're planning on using more than 1GB of RAM for minecraft, you should use the x64 version of Java!");
                }
            }
        } catch (Exception e) {
            Main.LOGGER.warning("Failed to validate Java, aborting launch!");
            e.printStackTrace();
            return;
        }

        // Launched as a separate process because Minecraft directly calls exit when quit is pressed.
        ProcessBuilder mcInit = new ProcessBuilder(args);
        mcInit.directory(new File(Config.getInstancePath(instance) + "/.minecraft"));

        Map<String, String> mcEnv = mcInit.environment();
        String newAppData = CommonConfig.getGlassPath() + "instances/" + instance;
        mcEnv.put("appdata", newAppData);
        mcEnv.put("home", newAppData);
        mcEnv.put("user.home", newAppData);
        mcEnv.put("fabric.gameJarPath", Config.getInstancePath(instance) + ".minecraft/bin/" + instJson.getVersion() + ".jar");

        try {
            Logger logger = LoggerFactory.makeLogger("Minecraft", "minecraft");
            logger.setUseParentHandlers(false);
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new MinecraftFormatter());
            logger.addHandler(consoleHandler);
            // Loggers made by LoggerFactory have their file handler added first.
            logger.getHandlers()[0].setFormatter(new MinecraftFormatter());

            (new Monitor(mcInit, (mc) -> {
                logger.info("Logging minecraft output to \"" + ((PublicPatternFileHandler) logger.getHandlers()[0]).publicPattern + "\"");
                MinecraftLogInterceptor mcStdout = new MinecraftLogInterceptor(mc.getInputStream(), logger, false);
                MinecraftLogInterceptor mcStderr = new MinecraftLogInterceptor(mc.getErrorStream(), logger, true);
                mcStdout.start();
                mcStderr.start();
            }, () -> {
                Main.LOGGER.info("Minecraft closed. Closing loggers.");
                for (Handler handler : logger.getHandlers()) {
                    handler.flush();
                    handler.close();
                }
                logger.removeHandler(consoleHandler);
                consoleHandler.close();
            })).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
