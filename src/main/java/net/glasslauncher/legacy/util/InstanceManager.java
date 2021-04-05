package net.glasslauncher.legacy.util;

import com.google.gson.Gson;
import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.FileUtils;
import net.glasslauncher.common.GenericInvalidVersionException;
import net.glasslauncher.common.JsonConfig;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.ProgressWindow;
import net.glasslauncher.legacy.VerifyAccountWindow;
import net.glasslauncher.legacy.jsontemplate.*;
import net.glasslauncher.proxy.web.WebUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;

public class InstanceManager {
    private static DownloadResourcesThreadManager downloadResourcesThreadManager;

    /**
     * Detects what kind of modpack zip has been provided and then calls the related install function for the type.
     * @param path Path to instance zip file.
     */
    public static void installModpack(String path, ProgressWindow progressWindow) {
        path = path.replace("\\", "/");
        Main.LOGGER.info("Installing " + path);
        boolean isURL = true;
        try {
            try {
                new URL(path);
            } catch (Exception e) {
                isURL = false;
            }

            String filename = path.substring(path.lastIndexOf('/') + 1);
            if (isURL) {
                FileUtils.downloadFile(path, Config.CACHE_PATH + "instancezips");
                installModpackZip(Config.CACHE_PATH + "instancezips/" + filename, filename, progressWindow);
            } else {
                if ((new File(path)).exists()) {
                    installModpackZip(path, filename, progressWindow);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void installModpackZip(String path, String filename, ProgressWindow progressWindow) {
        progressWindow.setProgress(0);
        progressWindow.setProgressMax(2);
        progressWindow.setProgressText("Initializing...");
        String instanceName = filename.replaceFirst("\\.jar$", "").replaceFirst("\\.zip$", "");
        TempZipFile instanceZipFile = new TempZipFile(path);
        try {
            progressWindow.setProgressText("Checking modpack type...");
            boolean isMultiMC = false;
            URL mmcPackURL = null;
            String mmcZipInstDir = "";

            // Because MMC makes a subfolder named after the instance and puts the instance files in there.
            for (File file : Objects.requireNonNull(instanceZipFile.getFile("").listFiles())) {
                if (file.isDirectory()) {
                    for (File subFile : Objects.requireNonNull(file.listFiles())) {
                        if (subFile.getName().equals("mmc-pack.json") && subFile.isFile()) {
                            isMultiMC = true;
                            mmcPackURL = subFile.toURI().toURL();
                            mmcZipInstDir = file.getName();
                        }
                    }
                }
            }

            if ((new File(CommonConfig.getGlassPath() + "instances/" + instanceName)).exists()) {
                Main.LOGGER.info("Instance \"" + instanceName + "\" already exists!");
                return;
            }
            if (isMultiMC) {
                progressWindow.increaseProgress();
                progressWindow.setProgressText("Installing MultiMC Modpack...");
                Main.LOGGER.info("Provided instance is a MultiMC instance. Importing...");
                InputStream inputStream = mmcPackURL.openStream();
                String jsonText = FileUtils.convertStreamToString(inputStream);
                inputStream.close();
                MultiMCPack multiMCPack = (new Gson()).fromJson(jsonText, MultiMCPack.class);
                importMultiMC(instanceZipFile, instanceName, mmcZipInstDir, multiMCPack, progressWindow);

            } else if (instanceZipFile.getFile("instance_config.json").exists()) {
                progressWindow.increaseProgress();
                progressWindow.setProgressText("Installing Glass Launcher Modpack...");
                Main.LOGGER.info("Provided instance is a Glass Launcher instance. Importing...");
                InstanceConfig instanceConfig = (InstanceConfig) JsonConfig.loadConfig(instanceZipFile.getFile("instance_config.json").getAbsolutePath(), InstanceConfig.class);
                createBlankInstance(instanceConfig.getVersion(), instanceName, progressWindow);
                if (new File(Config.getInstancePath(instanceName)).exists()) {
                    instanceZipFile.copyContentsToDir("", Config.getInstancePath(instanceName));
                }
                progressWindow.setProgressText("Installing mods...");
                ModList modList = (ModList) JsonConfig.loadConfig(Config.getInstancePath(instanceName) + "mods/mods.json", ModList.class);
                DefaultListModel<Mod> mods = new DefaultListModel<>();
                for (Mod mod : modList.getJarMods()) {
                    mods.add(mods.getSize(), mod);
                }
                addMods(instanceName, mods);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            instanceZipFile.close(false);
        }
    }

    public static void createBlankInstance(String version, String instance, ProgressWindow progressWindow) {
        progressWindow.setProgress(1);
        progressWindow.setProgressMax(4);
        progressWindow.setProgressText("Initializing...");
        if (!(new VerifyAccountWindow(progressWindow)).isLoginValid()) {
            JOptionPane.showMessageDialog(progressWindow, "Account not validated! Aborting.");
            return;
        }
        Main.LOGGER.info("Creating instance \"" + instance + "\" on version " + version);
        String versionsCachePath = Config.CACHE_PATH + "versions";
        String instanceFolder = Config.getInstancePath(instance);
        String minecraftFolder = instanceFolder + "/.minecraft";
        (new File(versionsCachePath)).mkdirs();
        (new File(minecraftFolder + "/bin/")).mkdirs();

        progressWindow.increaseProgress();
        progressWindow.setProgressText("Getting minecraft.jar for " + version + "...");
        File versionCacheJar = new File(versionsCachePath + "/" + version + ".jar");
        if (Config.getMcVersions().getClient().containsKey(version)) {
            if (versionCacheJar.exists()) {
                try {
                    Files.copy(versionCacheJar.toPath(), new File(minecraftFolder + "/bin/minecraft.jar").toPath());
                } catch (FileAlreadyExistsException e) {
                    Main.LOGGER.info("Instance \"" + instance + "\" already exists!");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        FileUtils.delete(new File(minecraftFolder));
                    } catch (Exception ex) {
                        e.printStackTrace();
                        ex.printStackTrace();
                    }
                    return;
                }
            } else {
                try {
                    String url = Config.getMcVersions().getClient().get(version).getUrl();
                    if (!(url.startsWith("https://") || url.startsWith("http://"))) {
                        url = "https://launcher.mojang.com/v1/objects/" + Config.getMcVersions().getClient().get(version).getUrl() + "/client.jar";
                    }
                    FileUtils.downloadFile(url, versionsCachePath, null, version + ".jar");
                    Files.copy(versionCacheJar.toPath(), new File(minecraftFolder + "/bin/minecraft.jar").toPath());
                } catch (Exception e) {
                    try {
                        FileUtils.delete(new File(minecraftFolder));
                    } catch (Exception ex) {
                        e.printStackTrace();
                        ex.printStackTrace();
                    }
                    return;
                }
            }
        }
        else if (!version.equals("custom")) {
            try {
                FileUtils.delete(new File(minecraftFolder));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        progressWindow.setProgressText("Creating instance config...");
        InstanceConfig instanceConfig = new InstanceConfig(instanceFolder + "instance_config.json");
        instanceConfig.setVersion(version);
        instanceConfig.saveFile();

        progressWindow.increaseProgress();
        progressWindow.setProgressText("Adding sounds...");
        addSounds(instance);
        progressWindow.increaseProgress();
        progressWindow.setProgressText("Adding LWJGL and JInput...");
        File lwjglCacheZip = new File(versionsCachePath + "/lwjgl.zip");
        if (!lwjglCacheZip.exists()) {
            FileUtils.downloadFile("https://files.pymcl.net/client/lwjgl/lwjgl." + Config.OS + ".zip", versionsCachePath, null, "lwjgl.zip");
        }
        FileUtils.extractZip(lwjglCacheZip.getPath(), minecraftFolder + "/bin");
    }

    public static void addMods(String instance, ListModel<Mod> mods) {
        instance = Config.getInstancePath(instance);
        try {
            File moddedJar = new File(instance, "/.minecraft/bin/minecraft.jar");
            File vanillaJar = new File(instance, "/.minecraft/bin/minecraft_vanilla.jar");
            if (moddedJar.exists() && !vanillaJar.exists()) {
                Files.move(moddedJar.toPath(), vanillaJar.toPath());
            }
            else if (moddedJar.exists()) {
                moddedJar.delete();
            }
            else {
                throw new FileNotFoundException("Minecraft.jar is missing! Restore it to fix this error!");
            }
            ArrayList<File> zips = new ArrayList<>();
            for (int i = mods.getSize(); i != 0; i--) {
                Main.LOGGER.info(instance + "mods/" + mods.getElementAt(i-1).getFileName());
                zips.add(new File(instance + "mods/" + mods.getElementAt(i-1).getFileName()));
            }
            zips.add(vanillaJar);
            FileUtils.mergeZips(moddedJar, zips);
            FileSystem jarFs = FileSystems.newFileSystem(moddedJar.toPath(), null);
            try {
                Files.delete(jarFs.getPath("META-INF/MOJANG_C.DSA"));
                Files.delete(jarFs.getPath("META-INF/MOJANG_C.SF"));
                Files.delete(jarFs.getPath("META-INF/MANIFEST.MF"));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            jarFs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void importMultiMC(TempZipFile mmcZip, String instance, String mmcZipInstDir, MultiMCPack multiMCPack, ProgressWindow progressWindow) throws GenericInvalidVersionException {
        String instPath = Config.getInstancePath(instance);
        InstanceConfig instanceConfig = new InstanceConfig(instPath + "instance_config.json");
        ModList modList = new ModList(instPath + "mods/mods.json");
        boolean hasCustomJar = false;

        if (multiMCPack.getFormatVersion() != 1) {
            throw new GenericInvalidVersionException("MultiMC instance version is unsupported!");
        }

        progressWindow.setProgress(0);
        progressWindow.setProgressMax(3);
        progressWindow.setProgressText("Generating mod list...");
        for (MultiMCComponent component : multiMCPack.getComponents()) {
            if (component.isImportant()) {
                instanceConfig.setVersion(component.getCachedVersion());
            }
            else if (component.isDependencyOnly() || (component.getUid().equals("customjar") && component.isDisabled())) {}
            else if (component.getUid().equals("customjar") && !component.isDisabled()) {
                hasCustomJar = true;
            }
            else if (component.getCachedName() != null) {
                modList.getJarMods().add(modList.getJarMods().size(), new Mod(component.getUid().replace("org.multimc.jarmod.", "") + ".jar", component.getCachedName(), !component.isDisabled(), new String[]{}, ""));
            }
        }

        createBlankInstance(instanceConfig.getVersion(), instance, progressWindow);
        if (!(new File(instPath)).exists()) {
            mmcZip.close(false);
            return;
        }

        progressWindow.setProgress(1);
        progressWindow.setProgressMax(3);
        progressWindow.setProgressText("Copying mods...");

        for (Mod mod : modList.getJarMods()) {
            try {
                org.apache.commons.io.FileUtils.copyFile(mmcZip.getFile(mmcZipInstDir + "/jarmods/" + mod.getFileName()), new File(instPath + "/mods/" + mod.getFileName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        progressWindow.setProgressText("Applying mods...");
        DefaultListModel<Mod> mods = new DefaultListModel<>();
        for (Mod mod : modList.getJarMods()) {
            mods.add(mods.getSize(), mod);
        }

        File vanillaJar = new File(instPath + ".minecraft/bin/minecraft_vanilla.jar");
        File moddedJar = new File(instPath + ".minecraft/bin/minecraft.jar");
        try {
            if (vanillaJar.exists()) {
                moddedJar.delete();
                Files.copy(vanillaJar.toPath(), moddedJar.toPath());
            } else {
                Files.copy(moddedJar.toPath(), vanillaJar.toPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        InstanceManager.addMods(instance, mods);

        try {
            progressWindow.setProgressText("Copying custom minecraft.jar...");
            if (hasCustomJar) {
                File originalJar = new File(instPath + ".minecraft/bin/minecraft.jar");
                originalJar.delete();
                Files.copy(mmcZip.getFile(mmcZipInstDir + "/libraries/customjar-1.jar").toPath(), originalJar.toPath());
                instanceConfig.setVersion("custom");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressWindow.increaseProgress();
        progressWindow.setProgressText("Saving config and copying instance files...");
        instanceConfig.saveFile();
        modList.saveFile();
        mmcZip.copyContentsToDir(mmcZipInstDir + "/.minecraft", instPath + ".minecraft");
    }

    private static void addSounds(String instance) {
        String basePath = Config.getInstancePath(instance) + ".minecraft/resources/";
        MinecraftResources minecraftResources;

        try {
            minecraftResources = (new Gson()).fromJson(WebUtils.getStringFromURL(Config.BASE_RESOURCES_URL + "json.php"), MinecraftResources.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        downloadResourcesThreadManager = new DownloadResourcesThreadManager(Arrays.asList(minecraftResources.getFiles()), basePath);
        downloadResourcesThreadManager.run();
    }
}
