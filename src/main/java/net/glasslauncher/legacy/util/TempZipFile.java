package net.glasslauncher.legacy.util;

import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TempZipFile {
    private final String originalPath;
    private final String tempPath;

    public TempZipFile(String zipFilePath) {
        File zipFile = new File(zipFilePath);
        originalPath = zipFilePath;
        tempPath = Config.getGlassPath() + "temp/" + zipFile.getName().replaceFirst("\\.zip$", "");
        (new File(tempPath)).mkdirs();

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
            for (File file : fileList) {
                byte[] data = Files.readAllBytes(Paths.get(tempPath + "/" + file));
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
                ZipEntry e = new ZipEntry(file.toPath().relativize((new File(tempPath)).toPath()).toString());
                out.putNextEntry(e);

                out.write(data, 0, data.length);
                out.closeEntry();

                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(tempPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
