package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public class ModCompatInfo {

    private Mod mod;
    private ArrayList<ClassInfo> classes = new ArrayList<>();

    public ModCompatInfo(Mod mod) {
        this.mod = mod;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ModCompatInfo && ((ModCompatInfo) other).getMod().getFileName().equals(getMod().getFileName());
    }
}
