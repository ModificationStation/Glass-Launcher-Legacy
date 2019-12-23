package net.glasslauncher.legacy.util;

import com.google.gson.Gson;
import net.glasslauncher.jsontemplate.*;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.proxy.web.WebUtils;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.Objects;

public class InstanceManager {

    /**
     * Detects what kind of modpack zip has been provided and then calls the related install function for the type.
     * @param path Path to instance zip file.
     */
    public static void installModpack(String path) {
        path = path.replace("\\", "/");
        Main.getLogger().info("Installing " + path);
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
                installModpackZip(Config.CACHE_PATH + "instancezips", filename);
            } else {
                if ((new File(path)).exists()) {
                    installModpackZip(path, filename);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void installModpackZip(String path, String filename) {
        String instanceName = filename.replaceFirst("\\.jar$", "");
        TempZipFile instanceZipFile = new TempZipFile(path);
        try {
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

            if (isMultiMC) {
                Main.getLogger().info("Provided instance is a MultiMC instance. Importing...");
                if ((new File(Config.GLASS_PATH + "instances/" + instanceName)).exists()) {
                    Main.getLogger().info("Instance \"" + instanceName + "\" already exists!");
                    return;
                }
                InputStream inputStream = mmcPackURL.openStream();
                String jsonText = FileUtils.convertStreamToString(inputStream);
                inputStream.close();
                MultiMCPack multiMCPack = (new Gson()).fromJson(jsonText, MultiMCPack.class);
                importMultiMC(instanceZipFile, instanceName, mmcZipInstDir, multiMCPack);

            } else if (instanceZipFile.getFile("instance_config.json").exists()) {
                InstanceConfig instanceConfig = new InstanceConfig(instanceZipFile.getFile("instance_config.json").getPath());
                createBlankInstance(instanceConfig.getVersion(), instanceName);
                instanceZipFile.copyContentsToDir("", Config.getInstancePath(instanceName));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            instanceZipFile.close(false);
        }
    }

    public static void createBlankInstance(String version, String instance) {
        Main.getLogger().info("Creating instance \"" + instance + "\" on version " + version);
        String versionsCachePath = Config.CACHE_PATH + "versions";
        String instanceFolder = Config.getInstancePath(instance);
        String minecraftFolder = instanceFolder + "/.minecraft";
        (new File(versionsCachePath)).mkdirs();
        (new File(minecraftFolder + "/bin/")).mkdirs();

        File versionCacheJar = new File(versionsCachePath + "/" + version + ".jar");
        if (Config.getMcVersions().getClient().containsKey(version)) {
            if (versionCacheJar.exists()) {
                try {
                    Files.copy(versionCacheJar.toPath(), new File(minecraftFolder + "/bin/minecraft.jar").toPath());
                } catch (FileAlreadyExistsException e) {
                    Main.getLogger().info("Instance \"" + instance + "\" already exists!");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        org.apache.commons.io.FileUtils.deleteDirectory(new File(minecraftFolder));
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
                        org.apache.commons.io.FileUtils.deleteDirectory(new File(minecraftFolder));
                    } catch (Exception ex) {
                        e.printStackTrace();
                        ex.printStackTrace();
                    }
                    return;
                }
            }
        }
        else if (version.equals("custom")) {}
        else {
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(new File(minecraftFolder));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        addSounds(instance);
        File lwjglCacheZip = new File(versionsCachePath + "/lwjgl.zip");
        if (!lwjglCacheZip.exists()) {
            FileUtils.downloadFile("https://files.pymcl.net/client/lwjgl/lwjgl." + Config.OS + ".zip", versionsCachePath, null, "lwjgl.zip");
        }
        FileUtils.extractZip(lwjglCacheZip.getPath(), minecraftFolder + "/bin");
    }

    public static void addMods(String instance, ListModel<Mod> mods) {
        instance = Config.GLASS_PATH + "instances/" + instance;
        try {
            TempZipFile jarFile = new TempZipFile(instance + "/.minecraft/bin/minecraft.jar");
            if (jarFile.fileExists("META-INF")) {
                jarFile.deleteFile("META-INF");
            }
            for (int i = 0; i < mods.getSize(); i++) {
                if (mods.getElementAt(i).isEnabled()) {
                    jarFile.mergeZip(instance + "/mods/" + mods.getElementAt(i).getFileName());
                }
            }
            jarFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void importMultiMC(TempZipFile mmcZip, String instance, String mmcZipInstDir, MultiMCPack multiMCPack) throws GenericInvalidVersionException{
        String instPath = Config.getInstancePath(instance);
        InstanceConfig instanceConfig = new InstanceConfig(instPath + "instance_config.json");
        ModList modList = new ModList(instPath + "mods/mods.json");
        boolean hasCustomJar = false;

        if (multiMCPack.getFormatVersion() != 1) {
            throw new GenericInvalidVersionException("MultiMC instance version is unsupported!");
        }
        for (MultiMCComponent component : multiMCPack.getComponents()) {
            if (component.isImportant()) {
                instanceConfig.setVersion(component.getCachedVersion());
            }
            else if (component.isDependencyOnly() || (component.getUid().equals("customjar") && component.isDisabled())) {}
            else if (component.getUid().equals("customjar") && !component.isDisabled()) {
                hasCustomJar = true;
            }
            else if (component.getCachedName() != null) {
                modList.getJarMods().add(modList.getJarMods().size(), new Mod(component.getUid().replace("org.multimc.jarmod.", "") + ".jar", component.getCachedName(), 0, !component.isDisabled()));
            }
        }
        createBlankInstance(instanceConfig.getVersion(), instance);

        for (Mod mod : modList.getJarMods()) {
            try {
                org.apache.commons.io.FileUtils.copyFile(mmcZip.getFile(mmcZipInstDir + "/jarmods/" + mod.getFileName()), new File(instPath + "/mods/" + mod.getFileName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            if (hasCustomJar) {
                File originalJar = new File(instPath + ".minecraft/bin/minecraft.jar");
                originalJar.delete();
                org.apache.commons.io.FileUtils.copyFile(mmcZip.getFile(mmcZipInstDir + "/libraries/customjar-1.jar"), originalJar);
                instanceConfig.setVersion("custom");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        instanceConfig.saveFile();
        modList.saveFile();
        mmcZip.copyContentsToDir(mmcZipInstDir + "/.minecraft", instPath + ".minecraft");
    }

    private static void addSounds(String instance) {
        String baseURL = "https://mcresources.modification-station.net/MinecraftResources/";
        String basePath = Config.getInstancePath(instance) + ".minecraft/resources/";
        MinecraftResources minecraftResources;

        try {
            minecraftResources = (new Gson()).fromJson(WebUtils.getStringFromURL(baseURL + "json.php"), MinecraftResources.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            for (MinecraftResource minecraftResource : minecraftResources.getFiles()) {
                File file = new File(basePath + minecraftResource.getFile());
                File cacheFile = new File(Config.GLASS_PATH + "cache/resources/" + minecraftResource.getFile());
                String md5 = minecraftResource.getMd5();
                String url = baseURL + minecraftResource.getFile().replace(" ", "%20");

                FileUtils.downloadFile(url, cacheFile.getParent(), md5);
                org.apache.commons.io.FileUtils.copyFile(cacheFile, file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
