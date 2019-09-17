package net.glass.glassl;

import java.io.File;
import java.util.HashMap;

public class Config {
    public static int proxyport = 25560;
    public static final String os = getOS();
    public static final String version = "v0.2";
    public static final String glasspath = getDataPath(".PyMCL");
    public static final String pymclpath = getDataPath(".PyMCL");
    public static final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    public static HashMap prettyprint = new HashMap(){{
        put("PRETTY_PRINT", true);
        put("TYPE", false);
    }};
    public static final String defaultjson = "{\n" +
            "    \"javaargs\": \"\",\n" +
            "    \"maxram\": \"512m\",\n" +
            "    \"minram\": \"256m\",\n" +
            "    \"proxycape\": false,\n" +
            "    \"proxyskin\": false,\n" +
            "    \"proxysound\": false\n" +
            "}";

    private static String getOS() {
        String os = (System.getProperty("os.name")).toLowerCase();
        if (os.contains("win")) {
            return "windows";
        }
        else if (os.contains("mac")) {
            return "osx";
        }
        else {
            return "unix";
        }
    }

    private static String getDataPath(String name) {
        if (os.equals("windows")) {
            return System.getenv("AppData") + "/" + name + "/";
        } else if (os.equals("osx")) {
            return System.getProperty("user.home") + "/Library/Application Support/" + name + "/";
        } else {
            return System.getProperty("user.home") + "/" + name + "/";
        }
    }
}
