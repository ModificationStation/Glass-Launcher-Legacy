package net.glasslauncher.legacy.jsontemplate;

import com.google.gson.annotations.Expose;
import lombok.Data;

@Data
public class Mod {
    @Expose private String fileName;
    @Expose private long type;
    @Expose private String name;
    @Expose private boolean enabled;

    public Mod(String modFileName, String modName, long modType, boolean modEnabled) {
        this.fileName = modFileName;
        this.type = modType;
        this.name = modName;
        this.enabled = modEnabled;
    }

    public String toString() {
        return name;
    }
}
