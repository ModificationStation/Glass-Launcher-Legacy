package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.components.events.OnModChange;

import javax.swing.*;
import java.awt.*;

public class LocalModDetailsPanel extends ModDetailsPanel {
    public LocalModDetailsPanel(String instance) {
        super(instance);
    }

    @Override
    void onModChange() {
        for (Component component : super.componentArrayList) {
            if (component instanceof OnModChange) {
                ((OnModChange) component).onLocalModChange(localMod);
            }
        }
    }

    @Override
    void setupButtons(JPanel buttons) {

    }
}
