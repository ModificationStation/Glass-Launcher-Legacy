package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;

import java.util.*;

@Getter
public class FabricMod {
    private String id;
    private String version;
    private String name;
    private String description;
    private String[] authors;
    private HashMap<String, String> contact;
    private String license;
    private String environment;
    private HashMap<String, String> depends;
}
