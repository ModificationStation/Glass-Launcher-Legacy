package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.common.JsonConfig;

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

    /**
     * @param path Path to the JSON file.
     */
    public InstanceConfig(String path) {
        super(path);
    }
}
