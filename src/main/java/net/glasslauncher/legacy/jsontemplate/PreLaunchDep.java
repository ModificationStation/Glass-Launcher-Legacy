package net.glasslauncher.legacy.jsontemplate;

import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

import static net.glasslauncher.common.FileUtils.downloadFile;

public class PreLaunchDep implements JsonPostProcessable {

    public PreLaunchDep(String name, String url, String customPath) {
        this.name = name;
        this.url = url;
        this.glassPath = customPath;
    }

    public PreLaunchDep(String name, String url) {
        this.name = name;
        this.url = url;
    }

    private String glassPath = Config.CACHE_PATH;
    private String name = "";
    private String url = "";
    private transient String group = "";
    private transient String module = "";
    private transient String version = "";
    private transient String extra = null;

    @Override
    public void jsonPostProcess() {
        String[] nameParts = name.split(":");
        group = nameParts[0];
        module = nameParts[1];
        version = nameParts[2];
        if (nameParts.length == 4) {
            extra = nameParts[3];
        }
    }

    public void cache() {
        try {
            URL url = new URL((this.url.endsWith("/")? this.url : this.url + "/") + group.replace(".", "/") + "/" + module + "/" + version + "/" + module + "-" + version + ".jar");
            URL md5Url = new URL(url + ".md5");
            String md5 = null;

            HttpURLConnection md5Connection = (HttpURLConnection) md5Url.openConnection();
            if (md5Connection.getResponseCode() != 200) {
                log("Can't get MD5 for maven dependency! Assuming dependency is valid.", true);
            }
            else {
                log("Found MD5!");
                Scanner s = (new Scanner(md5Connection.getInputStream())).useDelimiter("\\A");
                md5 = s.hasNext() ? s.next() : "";
            }

            File mavenCache = new File(glassPath, "prelaunch");
            mavenCache.mkdirs();
            downloadFile(url.toString(), mavenCache.getAbsolutePath(), md5, group + "-" + module + "-" + version + ".jar");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void log(String string) {
        log(string, false);
    }

    private static void log(String string, boolean warning) {
        if (Main.LOGGER != null) {
            if(warning) {
                Main.LOGGER.warning(string);
                return;
            }
            Main.LOGGER.info(string);
        }
        System.out.println(warning? "WARN: " + string : string);
    }


    // Copy-paste from glass-commons due to needing it before deps are loaded.
    private static void downloadFile(String urlStr, String pathStr, String md5, String filename) {
        URL url;
        log("Downloading " + urlStr + " to " + pathStr);
        try {
            url = new URL(urlStr);
        } catch (Exception var11) {
            log("Failed to download file \"" + urlStr + "\": Invalid URL.", true);
            var11.printStackTrace();
            return;
        }

        File file;
        try {
            (new File(pathStr)).mkdirs();
            file = new File(pathStr + "/" + filename);
            if (md5 != null && file.exists() && getFileChecksum(MessageDigest.getInstance("MD5"), file).toLowerCase().equals(md5.toLowerCase())) {
                return;
            }
        } catch (Exception var13) {
            log("Failed to download file \"" + urlStr + "\": Invalid path.", true);
            var13.printStackTrace();
            return;
        }

        try {
            log("Downloading \"" + urlStr + "\".");
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.connect();
            BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
            FileOutputStream fileOS = new FileOutputStream(file);
            byte[] data = new byte[1024];

            int byteContent;
            while((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                fileOS.write(data, 0, byteContent);
            }

            fileOS.close();
        } catch (Exception var12) {
            log("Failed to download file \"" + urlStr + "\":", true);
            var12.printStackTrace();
        }
    }

    // Copy-paste from glass-commons.
    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];

        int bytesCount;
        while((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        fis.close();
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        byte[] var7 = bytes;
        int var8 = bytes.length;

        for(int var9 = 0; var9 < var8; ++var9) {
            byte b = var7[var9];
            sb.append(Integer.toString((b & 255) + 256, 16).substring(1));
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
