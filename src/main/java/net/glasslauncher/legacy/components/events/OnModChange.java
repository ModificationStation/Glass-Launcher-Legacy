package net.glasslauncher.legacy.components.events;

import net.glasslauncher.legacy.jsontemplate.Mod;

public interface OnModChange {

    void onRepoModChange(net.glasslauncher.repo.api.mod.jsonobj.Mod mod);

    void onLocalModChange(Mod mod);
}
