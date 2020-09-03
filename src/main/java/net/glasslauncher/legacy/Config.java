package net.glasslauncher.legacy;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.JsonConfig;
import net.glasslauncher.legacy.jsontemplate.LauncherConfig;
import net.glasslauncher.legacy.jsontemplate.MCVersions;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    public static void loadConfigFiles() {
        Gson gson = new Gson();
        mcVersions = gson.fromJson(new InputStreamReader(Main.class.getResourceAsStream("assets/mcversions.json")), MCVersions.class);
        launcherConfig = (LauncherConfig) JsonConfig.loadConfig(CommonConfig.GLASS_PATH + "launcher_config.json", LauncherConfig.class);
        if (launcherConfig == null) {
            launcherConfig = new LauncherConfig(CommonConfig.GLASS_PATH + "launcher_config.json");
        }
    }

    public static String destDirBypass = "";

    @Getter private static MCVersions mcVersions;
    @Getter private static LauncherConfig launcherConfig;

    public static final long CACHE_AGE_LIMIT = 600L;

    /**
     * The port which the built-in proxy runs on.
     */
    public static final int PROXY_PORT = 25560;

    /**
     * The port which the built-in webserver for the proxy runs on.
     */
    public static final int PROXY_WEB_PORT = 25561;

    /**
     * The address which the built-in proxy and webserver runs on.
     */
    public static final String PROXY_ADDRESS = "127.0.0.1";

    /**
     * The hosts
     */
    public static final List<String> PROXY_IGNORED_HOSTS = Collections.unmodifiableList(new ArrayList<String>() {{
        add("pymcl.net");
        add("localhost");
        add("127.0.0.1");
        add("mojang.com");
        add("icebergcraft.com");
        add("betacraft.ovh");
        add("retrocraft.net");
        add("textures.minecraft.net");
        add("glass-launcher.net");
    }});

    /**
     * The current OS of the user.
     * @see #getOSString()
     */
    public static final String OS = getOSString();

    /**
     * The version of the launcher.
     */
    public static final String VERSION = "v0.4.5";

    /**
     * The path of the launcher's cache files.
     */
    public static final String CACHE_PATH = CommonConfig.GLASS_PATH + "cache/";

    /**
     * The path of the Java binary running the launcher.
     */
    public static final String JAVA_BIN = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

    /**
     * JSON format map that reduces clutter and makes the end JSON easy to read.
     */
    @Getter @Setter private static String easyMineLauncherFile;

    /**
     * Gets the OS of the user.
     * @return "windows" | "osx" | "unix"
     */
    private static String getOSString() {
        String os = (System.getProperty("os.name")).toLowerCase();
        if (os.contains("win")) {
            return "windows";
        } else if (os.contains("mac")) {
            return "osx";
        } else {
            return "linux";
        }
    }

    /**
     * Gets the %appdata% (or OS equivalent) folder of the given folder name.
     * @param name Data folder name.
     * @return A full path to the data folder.
     */
    private static String getDataPath(String name) {
        if (OS.equals("windows")) {
            return System.getenv("AppData").replaceAll("\\\\", "/") + "/" + name + "/";
        } else if (OS.equals("osx")) {
            return System.getProperty("user.home") + "/Library/Application Support/" + name + "/";
        } else {
            return System.getProperty("user.home") + "/" + name + "/";
        }
    }

    /**
     * Gets the path of the supplied instance.
     * @param instance Instance name.
     * @return Returns the full path of the instance.
     * @exception IllegalArgumentException Thrown when null or an empty string is supplied.
     */
    public static String getInstancePath(String instance) throws IllegalArgumentException {
        if (instance == null || instance.isEmpty()) {
            throw new IllegalArgumentException("Instance cannot be empty or null!");
        }
        return CommonConfig.GLASS_PATH + "instances/" + instance + "/";
    }

    static {
        if (Config.OS.equals("windows")) {
            destDirBypass = "\\\\?\\";
        }
    }
}
