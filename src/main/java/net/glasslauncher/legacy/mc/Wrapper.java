package net.glasslauncher.legacy.mc;

import net.fabricmc.loader.launch.knot.KnotClient;
import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.FileUtils;
import net.glasslauncher.common.JsonConfig;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.jsontemplate.InstanceConfig;
import net.glasslauncher.legacy.jsontemplate.MCVersion;
import net.glasslauncher.proxy.Proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Wrapper {
    private final String instance;

    private ArrayList<String> args;
    private InstanceConfig instJson;

    private Proxy proxy = null;

    public Wrapper(String[] launchArgs) {
        // 0: username, 1: session, 2: version, 3: doproxy, 4: instance
        if (launchArgs.length < 5) {
            Main.getLogger().severe("Got " + launchArgs.length + " args, expected 5.");
        }
        this.instance = launchArgs[4];
        String instPath = CommonConfig.GLASS_PATH + "instances/" + instance + "/.minecraft";

        this.getConfig();
        Map<String, MCVersion> mappings = Config.getMcVersions().getMappings();

        if (mappings.containsKey(instJson.getVersion())) {
            Main.getLogger().info("Downloading intermediary mappings for " + instJson.getVersion());
            FileUtils.downloadFile(mappings.get(instJson.getVersion()).getUrl(), Config.CACHE_PATH + "intermediary_mappings/", null, instJson.getVersion() + ".tiny");

            try {
                // input file
                FileInputStream in = new FileInputStream(Config.CACHE_PATH + "intermediary_mappings/" + instJson.getVersion() + ".tiny");

                // out put file
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(Config.CACHE_PATH + "intermediary_mappings/" + instJson.getVersion() + ".jar"));

                // name the file inside the zip  file
                out.putNextEntry(new ZipEntry("mappings/mappings.tiny"));

                // buffer size
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
            Main.getLogger().info("No intermediary mappings found for " + instJson.getVersion());
        }

        String extraCP = "";
        if (new File(Config.CACHE_PATH + "intermediary_mappings/" + instJson.getVersion() + ".jar").exists()) {
            Main.getLogger().info("Adding intermediary mappings for " + instJson.getVersion() + " to classpath.");
            extraCP = Config.CACHE_PATH + "intermediary_mappings/" + instJson.getVersion() + ".jar";
        }

        this.args = new ArrayList<>();
        args.add(Config.JAVA_BIN);
        if (launchArgs[3].equals("true")) {
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
        if (!javaArgs.isEmpty()) {
            for (String arg : javaArgs.split("- ")) {
                args.add("-" + arg);
            }
        }
        args.add("-Xmx" + instJson.getMaxRam());
        args.add("-Xms" + instJson.getMinRam());
        args.add("-Djava.library.path=" + instPath + "/bin/natives");
        args.add("-Dfabric.gameJarPath=" + instPath + "/bin/minecraft.jar");
        args.add("-cp");
        args.add(System.getProperty("java.class.path") + ";" + Config.getAbsolutePathForCP(instance, new String[] {
                ".minecraft/bin/minecraft.jar",
                ".minecraft/bin/lwjgl.jar",
                ".minecraft/bin/lwjgl_util.jar",
                ".minecraft/bin/jinput.jar",
                extraCP
        }));
        args.add(KnotClient.class.getCanonicalName());
        args.add("--gameDir=" + instPath + ".minecraft");
        args.add("--username=" + launchArgs[0]);
        args.add("--session=" + launchArgs[1]);
        args.add("--title=Minecraft " + launchArgs[2]);
    }

    private void getConfig() {
        String instPath = CommonConfig.GLASS_PATH + "instances/" + instance;
        File confFile = new File(instPath + "/instance_config.json");

        if (!confFile.exists()) {
            Main.getLogger().info("Config file does not exist! Using defaults.");
            instJson = new InstanceConfig(instPath + "/instance_config.json");
        } else {
            try {
                instJson = (InstanceConfig) JsonConfig.loadConfig(confFile.getPath(), InstanceConfig.class);
            } catch (Exception e) {
                Main.getLogger().info("Config file cannot be read! Using defaults.");
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
        String newAppData = CommonConfig.GLASS_PATH + "instances/" + instance;
        mcEnv.put("appdata", newAppData);
        mcEnv.put("home", newAppData);
        mcEnv.put("user.home", newAppData);
        mcEnv.put("fabric.gameJarPath", Config.getInstancePath(instance) + ".minecraft/bin/minecraft.jar");

        Process mc;
        try {
            mc = mcInit.start();
            StreamGobbler mcStdout = new StreamGobbler(mc.getInputStream(), System.out);
            StreamGobbler mcStderr = new StreamGobbler(mc.getErrorStream(), System.err);
            mcStdout.start();
            mcStderr.start();
            (new Monitor(mc, proxy)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
