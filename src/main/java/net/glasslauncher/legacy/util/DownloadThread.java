package net.glasslauncher.legacy.util;

import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DownloadThread extends Thread {
    private final String url;
    private final File cacheFile;
    private final File file;
    private final String md5;
    private volatile boolean completed = false;

    private Object dataLock = new Object();
    private Object data;


    public DownloadThread(String url, File cacheFile, File file, String md5) {
        this.url = url;
        this.cacheFile = cacheFile;
        this.file = file;
        this.md5 = md5;
    }


    @Override
    public void run() {
        try {
            FileUtils.downloadFile(url, cacheFile.getParent(), md5);
            file.getParentFile().mkdirs();
            Files.copy(cacheFile.toPath(), file.toPath());
//            System.out.println("Finished Downloading: " + url);
            synchronized (dataLock) {
                this.data = "COMPLETED";
                this.completed = true;
            }
        } catch (IOException exception) {
            synchronized (dataLock) {
                this.data = exception;
                this.completed = true;
            }
        }
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public Object getData() {
        return this.data;
    }
}