package net.glasslauncher.legacy.jsontemplate;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ModV2 {

    @SerializedName("MMC-displayname")
    private String displayname;
    @SerializedName("MMC-filename")
    private String filename;
    @SerializedName("MMC-hint")
    private String hint;
    private String name;
}
