package net.glasslauncher.legacy.mc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.glasslauncher.jsontemplate.InstanceConfig;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.util.FileUtils;
import net.glasslauncher.proxy.Proxy;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class Wrapper {
    private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
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
        String instPath = Config.getGlassPath() + "instances/" + instance + "/.minecraft";

        this.getConfig();

        this.args = new ArrayList<>();
        args.add(Config.getJavaBin());
        if (launchArgs[3].equals("true")) {
            args.add("-Dhttp.proxyHost=127.0.0.1");
            args.add("-Dhttp.proxyPort=" + Config.getProxyport());
            boolean[] proxyArgs = new boolean[]{
                    instJson.isProxySound(),
                    instJson.isProxySkin(),
                    instJson.isProxyCape()
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
        args.add("-jar");
        args.add(Config.getGlassPath() + "lib/" + Config.getEasyMineLauncherFile());
        args.add("--lwjgl-dir=" + instPath + "/bin");
        args.add("--jar=" + instPath + "/bin/minecraft.jar");
        args.add("--native-dir=" + instPath + "/bin/natives");
        args.add("--parent-dir=" + instPath);
        args.add("--height=520");
        args.add("--width=870");
        args.add("--username=" + launchArgs[0]);
        args.add("--session-id=" + launchArgs[1]);
        args.add("--title=" + launchArgs[2]);
    }

    private void getConfig() {
        String instPath = Config.getGlassPath() + "instances/" + instance;
        File confFile = new File(instPath + "/instance_config.json");

        if (!confFile.exists()) {
            Main.getLogger().info("Config file does not exist! Using defaults.");
            instJson = new InstanceConfig(instPath + "/instance_config.json");
        } else {
            try {
                instJson = gson.fromJson(FileUtils.readFile(confFile.getPath()), InstanceConfig.class);
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

        Map<String, String> mcEnv = mcInit.environment();
        String newAppData = Config.getGlassPath() + "instances/" + instance;
        mcEnv.put("appdata", newAppData);
        mcEnv.put("home", newAppData);
        mcEnv.put("user.home", newAppData);

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
