package net.glasslauncher.legacy.util;

import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class TempZipFile {
    private final String originalPath;
    private final String tempPath;

    public TempZipFile(String zipFilePath) {
        File zipFile = new File(zipFilePath);
        originalPath = zipFilePath;
        tempPath = Config.getGlassPath() + "temp/" + zipFile.getName().replaceFirst("\\.zip$", "");
        File tempFile = new File(tempPath);
        if (tempFile.exists()) {
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(tempFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tempFile.mkdirs();

        FileUtils.extractZip(zipFilePath, tempPath);
    }

    public void deleteFile(String relativePath) {
        File fileToDelete = new File(tempPath + "/" + relativePath);
        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }

    public boolean fileExists(String relativePath) {
        return (new File(tempPath + "/" + relativePath)).exists();
    }

    public void mergeZip(String zipToMergePath) {
        File zipToMerge = new File(zipToMergePath);
        if (zipToMerge.exists()) {
            FileUtils.extractZip(zipToMergePath, tempPath);
        }
    }

    public void close() {
        close(true);
    }

    public void close(boolean doSave) {
        File original = new File(originalPath);
        String destDirBypass = "";
        if (Config.getOs() == "windows") {
            destDirBypass = "\\\\?\\";
        }
        try {
            original.delete();
        } catch (Exception e) {
            Main.getLogger().info("Failed to delete/check read permissions for \"" + originalPath + "\"");
            e.printStackTrace();
            return;
        }
        try {
            File[] fileList = (new File(tempPath)).listFiles();
            if (fileList == null) {
                return;
            }
            File f = new File(originalPath);
            ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(f));
            addDir(new File(tempPath), zipFile);
            zipFile.flush();
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(destDirBypass + tempPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addDir(File sourceDir, ZipOutputStream zip) throws IOException {
        File[] contents = sourceDir.listFiles();
        for(File file : contents) {
            if(file.isDirectory()){
                addDir(file, zip);
            } else {
                zip.putNextEntry(new ZipEntry((Paths.get(tempPath).relativize(sourceDir.toPath()) + "/" + file.getName()).replaceAll("^/+", "")));
                Path rn_demo = Paths.get(String.valueOf(file));
                Files.copy(rn_demo, zip);
            }
        }
        zip.closeEntry();
    }
}
