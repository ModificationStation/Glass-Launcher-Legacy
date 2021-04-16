package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;

@Getter
public class ClassInfo {

    private String name;
    private boolean isMinecraft;

    public ClassInfo(String name) {
        this.name = name;
        this.isMinecraft = name.replace(".class", "").length() <= 2 || name.endsWith("net/minecraft/client/Minecraft.class");
    }
}
