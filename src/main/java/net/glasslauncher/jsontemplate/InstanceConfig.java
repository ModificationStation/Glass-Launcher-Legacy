package net.glasslauncher.jsontemplate;

import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.legacy.util.JsonConfig;

@Getter @Setter
public class InstanceConfig extends JsonConfig {
    private boolean proxySound = false;
    private boolean proxySkin = false;
    private boolean proxyCape = false;
    private boolean proxyLogin = false;
    private String maxRam = "512m";
    private String minRam = "64m";
    private String javaArgs = "";
    private String version = "none";

    /**
     * @param path Path to the JSON file.
     */
    public InstanceConfig(String path) {
        super(path);
    }
}
