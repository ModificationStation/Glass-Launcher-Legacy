package net.glasslauncher.legacy.mc;

import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.JsonConfig;
import net.glasslauncher.common.LoggerFactory;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.jsontemplate.InstanceConfig;
import net.glasslauncher.legacy.jsontemplate.MavenDep;
import net.glasslauncher.wrapper.LegacyWrapper;

import java.io.*;
import java.lang.reflect.*;
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

        dep = new MavenDep("com.github.calmilamsy:glass-launch-wrapper:0e96525", "https://jitpack.io");
        dep.jsonPostProcess();
        dep.cache();
        extraCP.append(";").append(dep.provide());
        if (new File(Config.CACHE_PATH + "intermediary_mappings/" + instJson.getVersion() + ".jar").exists()) {
            Main.LOGGER.info("Adding intermediary mappings for " + instJson.getVersion() + " to classpath.");
            extraCP.append(";").append(Config.CACHE_PATH).append("intermediary_mappings/").append(instJson.getVersion()).append(".jar");
        }
        for (MavenDep mavenDep : instJson.getMavenDeps()) {
            mavenDep.jsonPostProcess();
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
        args.add("-cp");
        args.add((Config.OS.equals("windows")? ";" : ":") + Config.getAbsolutePathForCP(instance, new String[] {
                ".minecraft/bin/" + instJson.getVersion() + ".jar",
                ".minecraft/bin/lwjgl.jar",
                ".minecraft/bin/lwjgl_util.jar",
                ".minecraft/bin/jinput.jar",
        }) + extraCP);
        args.add(LegacyWrapper.class.getCanonicalName());
        args.add("--path");
        args.add(instPath);
        args.add("--username");
        args.add(Config.getLauncherConfig().getLoginInfo().getUsername());
        args.add("--session");
        args.add(Config.getLauncherConfig().getLoginInfo().getAccessToken());
        args.add("--uuid");
        args.add(Config.getLauncherConfig().getLoginInfo().getUuid());
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
        // Launched as a separate process because Minecraft directly calls exit when quit is pressed.

        ProcessBuilder mcInit = new ProcessBuilder(args);
        mcInit.directory(new File(Config.getInstancePath(instance) + "/.minecraft"));

        Map<String, String> mcEnv = mcInit.environment();
        String newAppData = CommonConfig.getGlassPath() + "instances/" + instance;
        mcEnv.put("appdata", newAppData);
        mcEnv.put("home", newAppData);
        mcEnv.put("user.home", newAppData);
        mcEnv.put("fabric.gameJarPath", Config.getInstancePath(instance) + ".minecraft/bin/" + instJson.getVersion() + ".jar");
        System.out.println(Config.getInstancePath(instance) + ".minecraft/bin/" + instJson.getVersion() + ".jar");

        try {
            Logger logger = LoggerFactory.makeLogger("Minecraft", "minecraft");
            logger.setUseParentHandlers(false);
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new MinecraftFormatter());
            logger.addHandler(consoleHandler);
            logger.getHandlers()[0].setFormatter(new MinecraftFormatter());
            Field pattern = logger.getHandlers()[0].getClass().getDeclaredField("pattern");
            pattern.setAccessible(true);

            (new Monitor(mcInit, (mc) -> {
                try {
                    logger.info("Logging minecraft output to \"" + pattern.get(logger.getHandlers()[0]) + "\"");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
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
