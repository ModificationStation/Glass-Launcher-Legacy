package net.glasslauncher.legacy.jsontemplate;

public class LoaderMod extends Mod {

    public LoaderMod(String modFileName, String modName, String[] authors, String description) {
        super(modFileName, modName, true, authors, description);
    }

    @Override
    public boolean isEnabled() {
        return !getFileName().endsWith(".disabled");
    }
}
