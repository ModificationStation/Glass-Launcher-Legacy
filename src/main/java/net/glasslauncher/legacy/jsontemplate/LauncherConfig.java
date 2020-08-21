package net.glasslauncher.legacy.jsontemplate;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.common.JsonConfig;

@Getter @Setter
public class LauncherConfig extends JsonConfig {

    @SerializedName("lastusedname")
    private String lastUsedName = "";

    @SerializedName("lastusedinstance")
    private String lastUsedInstance = "";

    public LauncherConfig(String path) {
        super(path);
    }
}
