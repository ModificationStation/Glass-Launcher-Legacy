package net.glass.glassl;

public class Config {
    public static final String os = getOS();
    public static final String instpath = getInstPath();
    public static final String version = "v0.1";
    public static final String proxyport = "25560";
    static final String defaultjson = "{\n" +
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

    private static String getInstPath() {
        if (os.equals("windows")) {
            return System.getenv("AppData") + "/.PyMCL/";
        } else if (os.equals("osx")) {
            return System.getProperty("user.home") + "/Library/Application Support/.PyMCL/";
        } else {
            return System.getProperty("user.home") + "/.PyMCL/";
        }
    }
}
