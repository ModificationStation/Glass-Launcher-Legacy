package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.JsonConfig;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.wrapper.LegacyWrapper;

import java.util.*;

@Getter @Setter
public class InstanceConfig extends JsonConfig {
    private boolean proxySound = false;
    private boolean proxySkin = false;
    private boolean proxyCape = false;
    private boolean proxyLogin = false;
    private boolean proxyPiracyCheck = false;
    private String maxRam = "512m";
    private String minRam = "512m";
    private String javaArgs = "-XX:+UseG1GC -Dsun.rmi.dgc.server.gcInterval=2147483646 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";
    private String version = "none";
    private ArrayList<MavenDep> mavenDeps = new ArrayList<>();
    private String mainClass = "net.minecraft.client.Minecraft";
    private String customMinecraftArgs = "";
    private String customJava = "";
    private boolean hidingJavaWarnings = false;

    public String getCustomJava() {
        if (customJava != null && !customJava.isEmpty()) {
            return customJava;
        }
        else if (Config.getLauncherConfig().getJavaInstallPath() != null && !Config.getLauncherConfig().getJavaInstallPath().isEmpty()) {
            return Config.getLauncherConfig().getJavaInstallPath();
        }
        return CommonConfig.JAVA_BIN;
    }

    /**
     * @param path Path to the JSON file.
     */
    public InstanceConfig(String path) {
        super(path);
    }
}
