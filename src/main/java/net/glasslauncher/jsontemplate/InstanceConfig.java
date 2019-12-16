package net.glasslauncher.jsontemplate;

import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.legacy.util.JsonConfig;

@Getter @Setter
public class InstanceConfig extends JsonConfig {
    private boolean proxySound;
    private boolean proxySkin;
    private boolean proxyCape;
    private String maxRam;
    private String minRam;
    private String javaArgs;

    /**
     * @param path Path to the JSON file.
     */
    public InstanceConfig(String path) {
        super(path);
    }
}
