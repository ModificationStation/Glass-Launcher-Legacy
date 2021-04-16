package net.glasslauncher.legacy.jsontemplate;

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Mod {
    private String fileName;
    private long type;
    private String name;
    private boolean enabled;
    private String[] authors = new String[]{};
    private String description = "";

    public Mod(String modFileName, String modName, boolean modEnabled, String[] authors, String description) {
        this.fileName = modFileName;
        this.name = modName;
        this.enabled = modEnabled;
        this.authors = authors;
        this.description = description;
    }

    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Mod && ((Mod) other).getFileName().equals(getFileName());
    }
}
