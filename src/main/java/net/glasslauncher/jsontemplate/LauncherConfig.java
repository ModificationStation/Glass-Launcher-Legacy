package net.glasslauncher.jsontemplate;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.legacy.util.JsonConfig;

@Getter @Setter
public class LauncherConfig extends JsonConfig {
    public LauncherConfig(String path) {
        super(path);
    }

    @SerializedName("lastusedname")
    private String lastUsedName;
}
