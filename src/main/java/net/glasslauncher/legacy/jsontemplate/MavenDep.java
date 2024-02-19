package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;

import java.io.*;
import java.net.*;
import java.util.*;

import static net.glasslauncher.common.FileUtils.downloadFile;

@Getter
public class MavenDep implements JsonPostProcessable {

    public MavenDep(String name, String url, String customPath) {
        this.name = name;
        this.url = url;
        this.glassPath = customPath;
    }

    public MavenDep(String name, String url) {
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
        group = nameParts[0].replace(".", "/");
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

            File mavenCache = new File(glassPath, "maven/" + group);
            mavenCache.mkdirs();
            downloadFile(url.toString(), mavenCache.getAbsolutePath(), md5, module + "-" + version + ".jar");
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

    public File provide() {
        return new File(glassPath, "maven/" + group + "/" + module + "-" + version + ".jar");
    }
}
