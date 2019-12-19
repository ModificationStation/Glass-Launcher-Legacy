package net.glasslauncher.legacy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.jsontemplate.LauncherConfig;
import net.glasslauncher.jsontemplate.MCVersions;
import net.glasslauncher.legacy.util.JsonConfig;

import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Config {
    public static void loadConfigFiles() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        mcVersions = gson.fromJson(new InputStreamReader(Main.class.getResourceAsStream("assets/mcversions.json")), MCVersions.class);
        launcherConfig = (LauncherConfig) JsonConfig.loadConfig(Config.getGlassPath() + "launcher_config.json", LauncherConfig.class);
        if (launcherConfig == null) {
            launcherConfig = new LauncherConfig(Config.getGlassPath() + "launcher_config.json");
        }
    }

    @Getter private static MCVersions mcVersions;
    @Getter private static LauncherConfig launcherConfig;

    @Getter private static final long skinCacheAgeLimit = 600L;

    /**
     * The port which the built-in proxy runs on.
     */
    @Getter private static final int proxyport = 25560;

    /**
     * The port which the built-in webserver for the proxy runs on.
     */
    @Getter private static final int proxywebport = 25561;

    /**
     * The hosts
     */
    @Getter private static String[] proxyIgnoredHosts = new String[] {
            "pymcl.net",
            "localhost",
            "127.0.0.1",
            "mojang.com",
            "icebergcraft.com",
            "betacraft.ovh",
            "retrocraft.net",
            "textures.minecraft.net"
    };

    /**
     * The current OS of the user.
     * @see #getOSString()
     */
    @Getter private static final String os = getOSString();

    /**
     * The version of the launcher.
     */
    @Getter private static final String version = "v0.3";

    /**
     * The path of the launcher's files.
     * @see #getDataPath(String)
     */
    @Getter private static final String glassPath = getDataPath(".PyMCL");

    /**
     * The path of PyMCL's files.
     * Used for importing.
     * @see #getDataPath(String)
     */
    @Getter private static final String pymclPath = getDataPath(".PyMCL");

    /**
     * The path of the launcher's cache files.
     */
    @Getter private static final String cachePath = glassPath + "cache/";

    /**
     * The path of the Java binary running the launcher.
     */
    @Getter private static final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

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
        if (os.equals("windows")) {
            return System.getenv("AppData").replaceAll("\\\\", "/") + "/" + name + "/";
        } else if (os.equals("osx")) {
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
        return glassPath + "instances/" + instance + "/";
    }

    @Getter private static final HashMap<String, String> glassDeps = new HashMap<String, String>() {{
        put("http://easyminelauncher.bonsaimind.org/EasyMineLauncher_v1.0.jar", "D7873F0A7A97AD78DB711BAF7D24B795");
        put("https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar", "310f5841387183aca7900fead98d4858");
        put("https://repo1.maven.org/maven2/com/github/ganskef/littleproxy-mitm/1.1.0/littleproxy-mitm-1.1.0.jar", "B1FD7C2BFCD32BCF5873D298484DABBA");
        put("https://github.com/adamfisk/LittleProxy/releases/download/littleproxy-1.1.2/littleproxy-1.1.2-littleproxy-shade.jar", "05613C6D1BB1A8F826711BA54569311E");
    }};
}
