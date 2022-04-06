package net.glasslauncher.legacy.util;

import net.glasslauncher.common.FileUtils;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.jsontemplate.MinecraftResource;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class DownloadResourcesThreadManager {
    public static final int MAX_THREADS = 8;

    private final List<MinecraftResource> downloadData;
    private final File basePath;

    private boolean isThreadPoolDone = false;

    public DownloadResourcesThreadManager(List<MinecraftResource> downloadData, String basePath) {
        this.downloadData = downloadData;
        this.basePath = new File(basePath);
    }

    public void run() {
        ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_THREADS);
        for (MinecraftResource downloadData : downloadData) {
            try {
                pool.submit(() -> {
                    try {
                        File cacheFile = new File(Config.CACHE_PATH, "resources/" + downloadData.getFile());
                        File resourceFile = new File(basePath, downloadData.getFile());
                        boolean downloaded = FileUtils.downloadFile(downloadData.getUrl(), cacheFile.getParentFile().getAbsolutePath(), downloadData.getMd5(), cacheFile.getName());
                        if (downloaded || cacheFile.exists()) {
                            resourceFile.getParentFile().mkdirs();
                            Files.copy(cacheFile.toPath(), resourceFile.toPath());
                            Main.LOGGER.info("Finished downloading \"" + downloadData.getFile() + "\".");
                        } else {
                            Main.LOGGER.info("Skipped downloading \"" + downloadData.getFile() + "\" because of an error while downloading.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        pool.submit(() -> isThreadPoolDone = true);
        try {
            pool.shutdown();
            pool.awaitTermination(30, TimeUnit.MINUTES);
            if (!isThreadPoolDone) {
                Main.LOGGER.severe("It seems like the download thread took to long! Are you using dial-up?");
                Main.LOGGER.severe("Feel free to restart the download if so. Otherwise something could be broken.");
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Main.LOGGER.severe("Resources download cancelled!");
            // Not a perfect count, but this task is not exact anyways.
            Main.LOGGER.severe((pool.getActiveCount() + pool.getQueue().size()) + " download threads were cancelled.");
            pool.shutdownNow();
            e.printStackTrace();
        }
    }
}