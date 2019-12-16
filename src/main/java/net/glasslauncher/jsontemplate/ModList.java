package net.glasslauncher.jsontemplate;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.legacy.util.JsonConfig;

import java.util.Map;

@Getter @Setter
public class ModList extends JsonConfig {
    @SerializedName("jarmods")
    private Map<String, Mod> jarMods;

    /**
     * @param path Path to the JSON file.
     */
    public ModList(String path) {
        super(path);
    }
}
