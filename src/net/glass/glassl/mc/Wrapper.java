package net.glass.glassl.mc;

import net.glass.glassl.Config;
import net.glass.glassl.util.ComponentArrayList;

import java.util.ArrayList;
import java.util.Map;

import static net.glass.glassl.Main.logger;

public class Wrapper {
    private final String instance;
    private final String instPath;
    private final ComponentArrayList componentList;

    private ArrayList args;

    private Proxy proxy = null;

    public Wrapper(String[] launchArgs, ComponentArrayList componentList) {
        // 0: username, 1: session, 2: version, 3: doproxy, 4: instance
        if (launchArgs.length < 5) {
            logger.severe("Got " + launchArgs.length + " args, expected 5.");
        }
        this.instance = launchArgs[4];
        this.componentList = componentList;
        this.instPath = Config.glasspath + "instances/" + instance + "/.minecraft";

        this.args = new ArrayList();
        args.add(Config.javaBin);
        if (launchArgs[3].equals("true")) {
            args.add("-Dhttp.proxyHost=127.0.0.1");
            args.add("-Dhttp.proxyPort=" + Config.proxyport);
            boolean[] proxyArgs = new boolean[] {true, true, true};
            proxy = new Proxy(proxyArgs);
            proxy.start();
        }
        args.add("-jar");
        args.add("EasyMineLauncher.jar");
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

    public void startMC() {
        // Launched as a separate process because Minecraft directly calls exit when quit is pressed.
        componentList.setEnabledAll(false);

        ProcessBuilder mcInit = new ProcessBuilder(args);

        Map mcEnv = mcInit.environment();
        String newAppData = Config.glasspath + "instances/" + instance;
        mcEnv.put("appdata",  newAppData);
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
