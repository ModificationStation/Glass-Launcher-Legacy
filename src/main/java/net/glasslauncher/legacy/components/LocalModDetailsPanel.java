package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.components.templates.DetailsPanel;
import net.glasslauncher.legacy.jsontemplate.Mod;

import javax.swing.*;

public class LocalModDetailsPanel extends DetailsPanel {

    private Mod localMod = null;

    public LocalModDetailsPanel() {
        super();
    }

    @Override
    public void setMod(Object mod) {
        this.localMod = (Mod) mod;
        name.setText("<style>" +
                Config.getCSS() + "</style><body><div style=\"font-size: 18px;\">" +
                localMod.getName() + " <sup style=\"font-size: 10px;\">by " +
                localMod.getAuthors()[0] + "</sup></div></body>");
        description.setText(localMod.getDescription());

    }

    @Override
    public void setupButtons(JPanel buttons) {

    }
}
