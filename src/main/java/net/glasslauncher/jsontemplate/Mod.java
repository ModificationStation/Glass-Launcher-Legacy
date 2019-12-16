package net.glasslauncher.jsontemplate;

import lombok.Data;

@Data
public class Mod {
    private String fileName;
    private long type;
    private String name;
    private boolean enabled;

    public Mod(String modFileName, String modName, long modType, boolean modEnabled) {
        this.fileName = modFileName;
        this.type = modType;
        this.name = modName;
        this.enabled = modEnabled;
    }

    /**
     * Sets mod type.
     * 0: Jar mod
     * 1: Loader mod
     * @param modType 0-1(inclusive) int.
     */
    public void setType(long modType) {
        if (modType < 0 || modType > 1)
        this.type = modType;
    }

    public String toString() {
        return name;
    }
}
