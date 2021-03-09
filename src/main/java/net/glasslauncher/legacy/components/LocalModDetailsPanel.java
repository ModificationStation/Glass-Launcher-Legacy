package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.components.events.OnModChange;
import net.glasslauncher.legacy.jsontemplate.Mod;

import javax.swing.*;
import java.awt.*;

public class LocalModDetailsPanel extends ModDetailsPanel {

    private Mod localMod = null;

    public LocalModDetailsPanel(String instance) {
        super(instance);
    }

    @Override
    public void setMod(Object mod) {
        this.localMod = (Mod) mod;
        name.setText("<style>" +
                Config.CSS + "</style><body><div style=\"font-size: 18px;\">" +
                localMod.getName() + " <sup style=\"font-size: 10px;\">by " +
                localMod.getAuthors()[0] + "</sup></div></body>");
        description.setText(localMod.getDescription());

        onModChange();
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
