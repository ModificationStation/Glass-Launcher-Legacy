package net.glasslauncher.legacy;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.JsonConfig;
import net.glasslauncher.legacy.jsontemplate.LauncherConfig;
import net.glasslauncher.legacy.jsontemplate.MCVersions;

import java.io.*;
import java.net.*;
import java.util.*;

public class Config {

    /**
     * Used to fix an issue with windows where classes like aux.class are seen as system names.
     * Otherwise does nothing.
     */
    public static String destDirBypass = "";

    /**
     * Used to store Minecraft version download information.
     */
    @Getter private static MCVersions mcVersions;

    /**
     * Used to store the last username and instance used.
     */
    @Getter private static LauncherConfig launcherConfig;

    /**
     * The maximum age of a skin file in the proxy cache in seconds.
     */
    public static final long CACHE_AGE_LIMIT = 600L;

    /**
     * Hosts which are ignored by the proxy.
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
     * Base URL for use when downloading resources.
     * https://resourceproxy.pymcl.net/MinecraftResources/ is also an alternative in case the modstation mirror stops working.
     */
    public static final String BASE_RESOURCES_URL = "https://mcresources.modification-station.net/MinecraftResources/";

    /**
     * The current OS of the user.
     * @see #getOSString()
     */
    public static final String OS = getOSString();

    /**
     * The version of the launcher.
     */
    public static final String VERSION = Main.class.getPackage().getImplementationVersion() != null? Main.class.getPackage().getImplementationVersion() : "Dev Env";

    /**
     * The path of the launcher's cache files.
     */
    public static String CACHE_PATH;

    /**
     * The path of the Java binary running the launcher.
     */
    public static final String JAVA_BIN = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

    /**
     * JSON format map that reduces clutter and makes the end JSON easy to read.
     */
    @Getter @Setter private static String easyMineLauncherFile;

    /**
     * Loads various autogenerated config files.
     */
    public static void loadConfigFiles() {
        CACHE_PATH = CommonConfig.getGlassPath() + "cache/";
        Gson gson = new Gson();
        mcVersions = gson.fromJson(new InputStreamReader(Main.class.getResourceAsStream("assets/mcversions.json")), MCVersions.class);
        launcherConfig = (LauncherConfig) JsonConfig.loadConfig(CommonConfig.getGlassPath() + "launcher_config.json", LauncherConfig.class);
        if (launcherConfig == null) {
            Main.LOGGER.info("Generating new launcher config.");
            launcherConfig = new LauncherConfig(CommonConfig.getGlassPath() + "launcher_config.json");
        }
    }

    /**
     * CSS to de-windows95-ify html formatted areas.
     */
    public static String getCSS() {
        return "body {" +
                (getLauncherConfig().isThemeDisabled()? "" :
                "background-color: rgb(76, 76, 76);" +
                "color: #dadada;") +
                "padding-right: 10px;" +
                "padding-left: 10px;" +
                "word-break: break-all;" +
                "word-break: break-word;" +
                "font-family: -apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,\"Helvetica Neue\",Arial,\"Noto Sans\",sans-serif,\"Apple Color Emoji\",\"Segoe UI Emoji\",\"Segoe UI Symbol\",\"Noto Color Emoji\";" +
                "}" +
                "a {" +
                "color: rgb(0, 191, 255);" +
                "}";
    }

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
     * Returns all input files as a chain of absolute paths for use in -cp arguments.
     * @param instance The name of the instance
     * @param relPaths
     * @return
     */
    public static String getAbsolutePathForCP(String instance, String[] relPaths) {
        String instPath = getInstancePath(instance);
        ArrayList<String> fullPaths = new ArrayList<>();

        for (String path : relPaths) {
            fullPaths.add(instPath + path);
        }

        return String.join(OS.equals("windows")? ";" : ":", fullPaths);
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
        return CommonConfig.getGlassPath() + "instances/" + instance + "/";
    }

    public static URL msAuthURL;

    static {
        if (Config.OS.equals("windows")) {
            destDirBypass = "\\\\?\\";
        }
        try {
            msAuthURL = new URL("https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize" +
                    "?client_id=378f5e54-dc51-4d4d-ac0c-a3cceae485bf" +
                    "&response_type=code" +
                    "&redirect_uri=https://login.microsoftonline.com/common/oauth2/nativeclient" +
                    "&scope=XboxLive.signin%20offline_access");
        } catch (Exception e) {
            e.printStackTrace();
            Main.LOGGER.info("This should be impossible!");
        }
    }
}
