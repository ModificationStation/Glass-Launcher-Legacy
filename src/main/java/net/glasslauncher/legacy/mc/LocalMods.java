package net.glasslauncher.legacy.mc;

import com.google.gson.Gson;
import net.glasslauncher.common.FileUtils;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.jsontemplate.FabricMod;
import net.glasslauncher.legacy.jsontemplate.LoaderMod;
import net.glasslauncher.legacy.jsontemplate.Mod;

import java.io.*;
import java.net.*;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;

public class LocalMods {

    public static Mod getModInfo(String instPath, String modFileName) {
        URI modPath = URI.create("jar:" + new File(instPath.replace("\\", "/") + ".minecraft/mods/" + modFileName).toURI());
        Mod modInfo;

        try (FileSystem zipFile = FileSystems.newFileSystem(modPath, new HashMap<String, String>(){{ put("create", "false");}})) {
            Path zipPath = zipFile.getPath("fabric.mod.json");
            if (Files.exists(zipPath)) {
                FabricMod fabricMod = (new Gson()).fromJson(FileUtils.convertStreamToString(Files.newInputStream(zipPath)), FabricMod.class);

                StringBuilder dependencyText = new StringBuilder();
                for (String dependency : fabricMod.getDepends().keySet()) {
                    if (dependency.equals("minecraft")) {
                        dependencyText.append("<br><b>").append(dependency).append("</b>: ").append(fabricMod.getDepends().get(dependency).replace("1.0.0-beta", "b1").replace("1.0.0-alpha", "a1")).append(" ").append(fabricMod.getEnvironment().equals("*")? "client and server" : fabricMod.getEnvironment());
                    } else {
                        dependencyText.append("<br><b>").append(dependency).append("</b>: ").append(fabricMod.getDepends().get(dependency));
                    }
                }

                modInfo = new LoaderMod(
                        modFileName,
                        fabricMod.getName(),
                        fabricMod.getAuthors(),
                        fabricMod.getDescription() + "<br><br>" +
                                "<b style=\"font-size: 20px\">Dependencies</b>:" + dependencyText + "<br><br>" +
                                "<b style=\"font-size: 20px\">Authors</b>:<br>" + String.join("<br>", fabricMod.getAuthors())
                );
            } else {
                Main.LOGGER.info("No fabric.mod.json found in \"" + modFileName + "\"" );
                modInfo = new Mod(modFileName, (modFileName.endsWith(".zip") || modFileName.endsWith(".jar"))? modFileName.substring(0, modFileName.lastIndexOf('.')) : modFileName, true, new String[]{""}, "This mod has no fabric.mod.json file. This most likely means it is a ModLoader/Forge mod.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            modInfo = new Mod("", "", false, new String[]{""}, "");
        }

        return modInfo;
    }
}
