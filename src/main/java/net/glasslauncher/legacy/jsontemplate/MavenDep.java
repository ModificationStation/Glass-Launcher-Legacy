package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;
import net.glasslauncher.common.FileUtils;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;

import java.io.*;
import java.net.*;

@Getter
public class MavenDep implements JsonPostProcessable {

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
            Main.LOGGER.info(name);
            Main.LOGGER.info(url);
            URL url = new URL((this.url.endsWith("/")? this.url : this.url + "/") + group.replace(".", "/") + "/" + module + "/" + version + "/" + module + "-" + version + ".jar");
            URL md5Url = new URL(url + ".md5");
            String md5 = null;

            HttpURLConnection md5Connection = (HttpURLConnection) md5Url.openConnection();
            if (md5Connection.getResponseCode() != 200) {
                Main.LOGGER.warning("Can't get MD5 for maven dependency! Assuming dependency is valid.");
            }
            else {
                md5 = FileUtils.convertStreamToString(md5Connection.getInputStream());
            }

            File mavenCache = new File(Config.CACHE_PATH, "maven/" + group);
            mavenCache.mkdirs();
            FileUtils.downloadFile(url.toString(), mavenCache.getAbsolutePath(), md5, module + "-" + version + ".jar");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File provide() {
        return new File(Config.CACHE_PATH, "maven/" + group + "/" + module + "-" + version + ".jar");
    }
}
