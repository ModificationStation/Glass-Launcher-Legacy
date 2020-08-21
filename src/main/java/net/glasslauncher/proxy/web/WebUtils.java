package net.glasslauncher.proxy.web;

import com.google.gson.Gson;
import net.glasslauncher.legacy.jsontemplate.Profile;
import net.glasslauncher.legacy.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WebUtils {
    public static String getStringFromURL(String url) throws IOException {
        HttpURLConnection req = (HttpURLConnection) new URL(url).openConnection();
        BufferedReader res = new BufferedReader(new InputStreamReader(req.getInputStream()));
        StringBuilder resj = new StringBuilder();
        for (String strline = ""; strline != null; strline = res.readLine()) {
            resj.append(strline);
        }
        return resj.toString();
    }

    public static String getUUID(String username) throws IOException {
        Profile profile = (new Gson()).fromJson(getStringFromURL("https://api.mojang.com/users/profiles/minecraft/" + username + "?at=" + (new Date()).getTime() / 1000L), Profile.class);
        return profile.getId();
    }

    /**
     * Checks glass-launcher's cache folder for a given file/folder within age limit.
     *
     * @param path The relative path to the file/folder.
     * @return The file object if it was found and within age limit, else return null.
     */
    public static File checkCache(String path) {
        File file = new File(Config.CACHE_PATH + "webproxy/" + path);
        if (file.exists()) {
            BasicFileAttributes fileAttributes;
            try {
                fileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            if (fileAttributes.lastModifiedTime().to(TimeUnit.SECONDS) < (new Date().getTime()) / 1000L - Config.CACHE_AGE_LIMIT) {
                return null;
            }
            return file;
        }
        return null;
    }

    public static File getCache(String path) {
        return new File(Config.CACHE_PATH + "webproxy/" + path);
    }

    public static void makeCacheFolders(String path) {
        (new File(Config.CACHE_PATH + "webproxy/" + path)).mkdirs();
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
