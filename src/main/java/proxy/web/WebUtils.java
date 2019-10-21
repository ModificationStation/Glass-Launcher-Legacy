package proxy.web;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WebUtils {
    public static JsonObject getJsonFromURL(String url) throws IOException {
        HttpURLConnection req = (HttpURLConnection) new URL(url).openConnection();
        BufferedReader res = new BufferedReader(new InputStreamReader(req.getInputStream()));
        StringBuilder resj = new StringBuilder();
        for (String strline = ""; strline != null; strline = res.readLine()) {
            resj.append(strline);
        }
        Main.logger.info(url);
        Main.logger.info(resj.toString());
        return (JsonObject) JsonReader.jsonToJava(resj.toString());
    }

    public static String getUUID(String username) throws IOException {
        JsonObject profile = getJsonFromURL("https://api.mojang.com/users/profiles/minecraft/" + username + "?at=" + (new Date()).getTime() / 1000L);
        return (String) profile.get("id");
    }

    /**
     * Checks glass-launcher's cache folder for a given file/folder within age limit.
     *
     * @param path The relative path to the file/folder.
     * @return The file object if it was found and within age limit, else return null.
     */
    public static File checkCache(String path) {
        File file = new File(Config.cachepath + "webproxy/" + path);
        if (file.exists()) {
            BasicFileAttributes fileAttributes;
            try {
                fileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            if (fileAttributes.lastModifiedTime().to(TimeUnit.SECONDS) < (new Date().getTime()) / 1000L - Config.skinCacheAgeLimit) {
                return null;
            }
            return file;
        }
        return null;
    }

    public static File getCache(String path) {
        return new File(Config.cachepath + "webproxy/" + path);
    }

    public static void makeCacheFolders(String path) {
        (new File(Config.cachepath + "webproxy/" + path)).mkdirs();
    }

    public static void putCache(File file, byte[] bytes) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        if (bytes == null) {
            bytes = new byte[]{};
        }
        out.write(bytes);
        out.close();
    }

}
