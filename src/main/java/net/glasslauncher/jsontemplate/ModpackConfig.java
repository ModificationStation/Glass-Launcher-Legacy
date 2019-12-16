package net.glasslauncher.jsontemplate;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class ModpackConfig {
    private String version;
    @SerializedName("modpackname")
    private String modpackName;
    @SerializedName("mcver")
    private String mcVer;
}
