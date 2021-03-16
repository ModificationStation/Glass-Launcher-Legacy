package net.glasslauncher.legacy.mc;

import net.fabricmc.loader.launch.knot.KnotClient;
import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.FileUtils;
import net.glasslauncher.common.JsonConfig;
import net.glasslauncher.common.LoggerFactory;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.jsontemplate.InstanceConfig;
import net.glasslauncher.legacy.jsontemplate.MCVersion;
import net.glasslauncher.proxy.Proxy;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;
import java.util.zip.*;

public class Wrapper {
    private final String instance;

    private final ArrayList<String> args;
    private InstanceConfig instJson;

    private Proxy proxy = null;

    private final Logger logger = LoggerFactory.makeLogger("Minecraft", "minecraft");

    public Wrapper() {
        this.instance = Config.getLauncherConfig().getLastUsedInstance();
        String instPath = CommonConfig.getGlassPath() + "instances/" + instance + "/.minecraft";

        this.getConfig();
        Map<String, MCVersion> mappings = Config.getMcVersions().getMappings();

        if (mappings.containsKey(instJson.getVersion())) {
            Main.LOGGER.info("Downloading intermediary mappings for " + instJson.getVersion());
            FileUtils.downloadFile(mappings.get(instJson.getVersion()).getUrl(), Config.CACHE_PATH + "intermediary_mappings/", null, instJson.getVersion() + ".tiny");

            try {
                FileInputStream in = new FileInputStream(Config.CACHE_PATH + "intermediary_mappings/" + instJson.getVersion() + ".tiny");
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(Config.CACHE_PATH + "intermediary_mappings/" + instJson.getVersion() + ".jar"));
                out.putNextEntry(new ZipEntry("mappings/mappings.tiny"));

                byte[] b = new byte[1024];
                int count;

                while ((count = in.read(b)) > 0) {
                    out.write(b, 0, count);
                }
                out.close();
                in.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else {
            Main.LOGGER.info("No intermediary mappings found for " + instJson.getVersion());
        }

        String extraCP = "";
        if (new File(Config.CACHE_PATH + "intermediary_mappings/" + instJson.getVersion() + ".jar").exists()) {
            Main.LOGGER.info("Adding intermediary mappings for " + instJson.getVersion() + " to classpath.");
            extraCP = ";" + Config.CACHE_PATH + "intermediary_mappings/" + instJson.getVersion() + ".jar";
        }

        this.args = new ArrayList<>();
        args.add(Config.JAVA_BIN);
        if (instJson.isProxySound() || instJson.isProxyCape() || instJson.isProxySkin() || instJson.isProxyLogin()) {
            args.add("-Dhttp.proxyHost=127.0.0.1");
            args.add("-Dhttp.proxyPort=" + Config.PROXY_PORT);
            boolean[] proxyArgs = new boolean[]{
                    instJson.isProxySound(),
                    instJson.isProxySkin(),
                    instJson.isProxyCape(),
                    instJson.isProxyLogin()
            };
            proxy = new Proxy(proxyArgs);
            proxy.start();
        }
        String javaArgs = instJson.getJavaArgs();
        // TODO: Make user args better to use
        if (!javaArgs.isEmpty()) {
            for (String arg : javaArgs.split(" -")) {
                args.add("-" + arg);
            }
        }
        args.add("-Xmx" + instJson.getMaxRam());
        args.add("-Xms" + instJson.getMinRam());
        args.add("-Djava.library.path=" + instPath + "/bin/natives");
        args.add("-Dfabric.gameJarPath=" + instPath + "/bin/minecraft.jar");
        args.add("-cp");
        args.add(System.getProperty("java.class.path") + (Config.OS.equals("windows")? ";" : ":") + Config.getAbsolutePathForCP(instance, new String[] {
                ".minecraft/bin/minecraft.jar",
                ".minecraft/bin/lwjgl.jar",
                ".minecraft/bin/lwjgl_util.jar",
                ".minecraft/bin/jinput.jar",
        }) + extraCP);
        args.add(KnotClient.class.getCanonicalName());
        args.add("--gameDir");
        args.add(instPath);
        args.add("--username");
        args.add(Config.getLauncherConfig().getLoginInfo().getUsername());
        args.add("--session");
        args.add(Config.getLauncherConfig().getLoginInfo().getAccessToken());
        if (instJson.getVersion() != null && !instJson.getVersion().toLowerCase().equals("none")) {
            args.add("--title=Minecraft " + instJson.getVersion());
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
        mcEnv.put("fabric.gameJarPath", Config.getInstancePath(instance) + ".minecraft/bin/minecraft.jar");

        try {
            logger.setUseParentHandlers(false);
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new MinecraftFormatter());
            logger.addHandler(consoleHandler);
            logger.getHandlers()[0].setFormatter(new MinecraftFormatter());
            Field pattern = logger.getHandlers()[0].getClass().getDeclaredField("pattern");
            pattern.setAccessible(true);
            logger.info("Logging minecraft output to \"" + pattern.get(logger.getHandlers()[0]) + "\"");
            Process mc = mcInit.start();
            MinecraftLogInterceptor mcStdout = new MinecraftLogInterceptor(mc.getInputStream(), logger, false);
            MinecraftLogInterceptor mcStderr = new MinecraftLogInterceptor(mc.getErrorStream(), logger, true);
            mcStdout.start();
            mcStderr.start();

            (new Monitor(mc, proxy, () -> {
                logger.removeHandler(consoleHandler);
                consoleHandler.close();
            })).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
