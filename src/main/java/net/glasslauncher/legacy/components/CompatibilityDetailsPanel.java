package net.glasslauncher.legacy.components;

import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.components.templates.DetailsPanel;
import net.glasslauncher.legacy.jsontemplate.ClassInfo;
import net.glasslauncher.legacy.jsontemplate.Mod;
import net.glasslauncher.legacy.jsontemplate.ModCompatInfo;

import javax.swing.*;
import java.util.HashMap;

public class CompatibilityDetailsPanel extends DetailsPanel {
    private final HashMap<Mod, HashMap<Mod, ModCompatInfo>> compatInfo;

    public CompatibilityDetailsPanel(HashMap<Mod, HashMap<Mod, ModCompatInfo>> compatInfo) {
        super();
        this.compatInfo = compatInfo;
    }

    @Override
    public void setMod(Object object) {
        Mod mod = (Mod) object;
        HashMap<Mod, ModCompatInfo> modCompatInfoMap = compatInfo.get(mod);
        StringBuilder warnings = new StringBuilder();
        warnings.append("<a style=\"color: rgb(255, 153, 51)\">■ </a>= conflicting base edit<br><a style=\"color: rgb(214, 41, 41)\">■ </a>= conflicting mod file<br>Class type autodetection is not 100% accurate.<br>");
        for (Mod modKey : modCompatInfoMap.keySet()) {
            ModCompatInfo modCompatInfo = modCompatInfoMap.get(modKey);
            warnings.append("<h3>").append(modKey.getFileName()).append("</h3>");
            for (ClassInfo classInfo : modCompatInfo.getClasses()) {
                if (classInfo.isMinecraft()) {
                    warnings.append("<p style=\"margin-top: 3px\"><a style=\"color: rgb(255, 153, 51)\">■ </a>").append(classInfo.getName()).append("</p>");
                }
                else {
                    warnings.append("<p style=\"margin-top: 3px\"><a style=\"color: rgb(214, 41, 41)\">■ </a>").append(classInfo.getName()).append("</p>");
                }
            }
            warnings.append("<br>");
        }
        name.setText("<style>" + Config.getCSS() + "</style><body><div style=\"font-size: 18px;\">" +
                mod.getFileName() + (mod.getAuthors().length > 0? "<sup style=\"font-size: 10px;\">by " +
                mod.getAuthors()[0] + "</sup>" : "") + "</div></body>");
        description.setText(warnings.toString());
    }

    @Override
    public void setupButtons(JPanel buttons) {
    }
}
