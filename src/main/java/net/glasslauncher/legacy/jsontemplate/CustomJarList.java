package net.glasslauncher.legacy.jsontemplate;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter @Setter
public class CustomJarList {
    @Expose private List<ModV2> jarMods = new ArrayList<>();
}
