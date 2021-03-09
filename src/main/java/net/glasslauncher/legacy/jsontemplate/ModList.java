package net.glasslauncher.legacy.jsontemplate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.common.JsonConfig;

import java.util.*;

@Getter @Setter
public class ModList extends JsonConfig {
    @SerializedName("jarmods")
    @Expose private List<Mod> jarMods = new ArrayList<>();

    /**
     * @param path Path to the JSON file.
     */
    public ModList(String path) {
        super(path);
    }
}
