package net.glasslauncher.legacy.jsontemplate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.common.JsonConfig;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class CustomJarList {
    @Expose private List<ModV2> jarMods = new ArrayList<>();
}
