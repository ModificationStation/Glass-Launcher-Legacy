package net.glasslauncher.legacy.mc;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import static net.glasslauncher.legacy.Main.logger;

public class Wrapper {
    private final String instance;
    private final String instPath;

    private ArrayList args;
    private JsonObject instJson;

    private Proxy proxy = null;

    public Wrapper(String[] launchArgs) {
        // 0: username, 1: session, 2: version, 3: doproxy, 4: instance
        if (launchArgs.length < 5) {
            logger.severe("Got " + launchArgs.length + " args, expected 5.");
        }
        this.instance = launchArgs[4];
        this.instPath = Config.glasspath + "instances/" + instance + "/.minecraft";

        this.getConfig();

        this.args = new ArrayList();
        args.add(Config.javaBin);
        if (launchArgs[3].equals("true")) {
            args.add("-Dhttp.proxyHost=127.0.0.1");
            args.add("-Dhttp.proxyPort=" + Config.proxyport);
            boolean[] proxyArgs = new boolean[]{
                    (boolean) instJson.get("proxysound"),
                    (boolean) instJson.get("proxyskin"),
                    (boolean) instJson.get("proxycape")
            };
            proxy = new Proxy(proxyArgs);
            proxy.start();
        }
        String javaArgs = instJson.get("javaargs").toString();
        if (!javaArgs.isEmpty()) {
            for (String arg : javaArgs.split("- ")) {
                args.add("-" + arg);
            }
        }
        args.add("-Xmx" + instJson.get("maxram"));
        args.add("-Xms" + instJson.get("minram"));
        args.add("-jar");
        args.add(Config.glasspath + "lib/" + Config.easyMineLauncherFile);
        args.add("--lwjgl-dir=" + instPath + "/bin");
        args.add("--jar=" + instPath + "/bin/minecraft.jar");
        args.add("--native-dir=" + instPath + "/bin/natives");
        args.add("--parent-dir=" + instPath);
        args.add("--height=520");
        args.add("--width=870");
        args.add("--username=" + launchArgs[0]);
        args.add("--session-id=" + launchArgs[1]);
        args.add("--title=" + launchArgs[2]);
        logger.info(args.toString());
    }

    private void getConfig() {
        String instPath = Config.glasspath + "instances/" + instance;
        File confFile = new File(instPath + "/instance_config.json");

        if (!confFile.exists()) {
            logger.info("Config file does not exist! Using defaults.");
            instJson = (JsonObject) JsonReader.jsonToJava(Config.defaultjson);
        } else {
            try {
                instJson = (JsonObject) JsonReader.jsonToJava(FileUtils.readFile(confFile.getPath()));
            } catch (Exception e) {
                logger.info("Config file cannot be read! Using defaults.");
                instJson = (JsonObject) JsonReader.jsonToJava(Config.defaultjson);
                e.printStackTrace();
            }
        }
    }

    public void startMC() {
        // Launched as a separate process because Minecraft directly calls exit when quit is pressed.

        ProcessBuilder mcInit = new ProcessBuilder(args);

        Map mcEnv = mcInit.environment();
        String newAppData = Config.glasspath + "instances/" + instance;
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
