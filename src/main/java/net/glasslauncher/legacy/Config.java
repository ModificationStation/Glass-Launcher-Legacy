package net.glasslauncher.legacy;

import java.io.File;
import java.util.HashMap;

public class Config {
    public static long skinCacheAgeLimit = 600L;
    /**
     * The port which the built-in proxy runs on.
     */
    public static int proxyport = 25560;
    /**
     * The current OS of the user.
     * @see #getOS()
     */
    public static final String os = getOS();
    /**
     * The version of the launcher.
     */
    public static final String version = "v0.3";
    /**
     * The path of the launcher's files.
     * @see #getDataPath(String)
     */
    public static final String glasspath = getDataPath(".PyMCL");
    /**
     * The path of PyMCL's files.
     * Used for importing.
     * @see #getDataPath(String)
     */
    public static final String pymclpath = getDataPath(".PyMCL");
    /**
     * The path of the launcher's cache files.
     */
    public static final String cachepath = glasspath + "cache/";
    /**
     * The path of the Java binary running the launcher.
     */
    public static final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    /**
     * JSON format map that reduces clutter and makes the end JSON easy to read.
     */
    public static String easyMineLauncherFile;

    public static HashMap prettyprint = new HashMap() {{
        put("PRETTY_PRINT", true);
        put("TYPE", false);
    }};
    /**
     * Default JSON encoded string that is used for new or unreadable instance JSONs.
     */
    public static final String defaultjson = "{\n" +
            "    \"javaargs\": \"\",\n" +
            "    \"maxram\": \"512m\",\n" +
            "    \"minram\": \"256m\",\n" +
            "    \"proxycape\": false,\n" +
            "    \"proxyskin\": false,\n" +
            "    \"proxysound\": false\n" +
            "}";

    /**
     * Gets the OS of the user.
     * @return "windows" | "osx" | "unix"
     */
    private static String getOS() {
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
            return System.getenv("AppData") + "/" + name + "/";
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
        return glasspath + "instances/" + instance + "/";
    }
}
