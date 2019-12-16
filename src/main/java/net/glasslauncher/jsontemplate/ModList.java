package net.glasslauncher.jsontemplate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.legacy.util.JsonConfig;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class ModList extends JsonConfig {
    @SerializedName("jarmods")
    @Expose private Map<String, Mod> jarMods = new HashMap<>();

    /**
     * @param path Path to the JSON file.
     */
    public ModList(String path) {
        super(path);
    }
}
