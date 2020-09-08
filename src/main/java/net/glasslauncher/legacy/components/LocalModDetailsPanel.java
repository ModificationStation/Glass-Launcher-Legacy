package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.components.events.OnModChange;

import java.awt.Component;

public class LocalModDetailsPanel extends ModDetailsPanel {
    @Override
    void onModChange() {
        for (Component component : super.componentArrayList) {
            if (component instanceof OnModChange) {
                ((OnModChange) component).onLocalModChange(localMod);
            }
        }
    }
}
