package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;
import lombok.ToString;
import net.glasslauncher.legacy.Config;

@Getter @ToString
public class MinecraftResource {
    private String file;
    private String date;
    private long size;
    private String md5;

    public String getUrl() {
        return Config.BASE_RESOURCES_URL + file.replace(" ", "%20");
    }
}
