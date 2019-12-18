package net.glasslauncher.jsontemplate;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.legacy.util.JsonConfig;

@Getter @Setter
public class InstanceConfig extends JsonConfig {
    @Expose private boolean proxySound = false;
    @Expose private boolean proxySkin = false;
    @Expose private boolean proxyCape = false;
    @Expose private boolean proxyLogin = false;
    @Expose private String maxRam = "512m";
    @Expose private String minRam = "64m";
    @Expose private String javaArgs = "";

    /**
     * @param path Path to the JSON file.
     */
    public InstanceConfig(String path) {
        super(path);
    }
}
