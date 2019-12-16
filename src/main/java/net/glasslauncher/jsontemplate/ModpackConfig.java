package net.glasslauncher.jsontemplate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class ModpackConfig {
    @Expose private String version = "";
    @SerializedName("modpackname")
    @Expose private String modpackName;
    @SerializedName("mcver")
    @Expose private String mcVer = "b1.7.3";
}
