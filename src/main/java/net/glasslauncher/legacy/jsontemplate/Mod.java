package net.glasslauncher.legacy.jsontemplate;

import com.google.gson.annotations.Expose;
import lombok.Data;

@Data
public class Mod {
    @Expose private String fileName;
    @Expose private long type;
    @Expose private String name;
    @Expose private boolean enabled;
    @Expose private String[] authors = new String[]{};
    @Expose private String description = "";

    public Mod(String modFileName, String modName, long modType, boolean modEnabled, String[] authors, String description) {
        this.fileName = modFileName;
        this.type = modType;
        this.name = modName;
        this.enabled = modEnabled;
        this.authors = authors;
        this.description = description;
    }

    public String toString() {
        return name;
    }
}
