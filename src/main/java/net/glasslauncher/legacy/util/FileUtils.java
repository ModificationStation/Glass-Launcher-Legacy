package net.glasslauncher.legacy.util;

import net.glasslauncher.legacy.Main;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileUtils {

    public static String readFile(String path) throws IOException {
        return readFile(path, false);
    }

    public static String readFile(String path, boolean isJar) throws IOException {
        byte[] encoded;
        if (isJar) {
            String resLocation = path.split("(!)(?!.*\1)")[1].replaceAll("\\\\", "/");
            encoded = convertStreamToString(Main.class.getResourceAsStream(resLocation)).getBytes(StandardCharsets.UTF_8);
        } else {
            encoded = Files.readAllBytes(Paths.get(path.replaceAll("\\\\", "/")));
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    /**
     * Downloads given URL to target pathStr.
     * @param urlStr File to download.
     * @param pathStr Path to save the file to (filename decided by URL).
     * @return false if no file was downloaded, true if otherwise.
     */
    public static boolean downloadFile(String urlStr, String pathStr) {
        return downloadFile(urlStr, pathStr, null);
    }


    /**
     * Downloads given URL to target pathStr.
     * @param urlStr File to download.
     * @param pathStr Path to save the file to (filename decided by URL).
     * @param md5 MD5 to compare against. Ignored if null.
     * @return false if no file was downloaded if it was meant to be, true if otherwise.
     */
    public static boolean downloadFile(String urlStr, String pathStr, String md5) {
        String filename;
        try {
            filename = URLDecoder.decode(urlStr.substring(urlStr.lastIndexOf('/') + 1), StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return downloadFile(urlStr, pathStr, md5, filename);
    }

    /**
     * Downloads given URL to target path.
     * @param urlStr File to download.
     * @param pathStr Path to save the file to.
     * @param md5 MD5 to compare against. Ignored if null.
     * @return false if no file was downloaded if it was meant to be, true if otherwise.
     */
    public static boolean downloadFile(String urlStr, String pathStr, String md5, String filename) {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (Exception e) {
            Main.getLogger().info("Failed to download file \"" + urlStr + "\": Invalid URL.");
            e.printStackTrace();
            return false;
        }
        File file;
        try {
            (new File(pathStr)).mkdirs();
            file = new File(pathStr + "/" + filename);
            if (md5 != null && file.exists() && getFileChecksum(MessageDigest.getInstance("MD5"), file).toLowerCase().equals(md5.toLowerCase())) {
                return true;
            }
        } catch (Exception e) {
            Main.getLogger().info("Failed to download file \"" + urlStr + "\": Invalid path.");
            e.printStackTrace();
            return false;
        }

        try {
            Main.getLogger().info("Downloading \"" + urlStr + "\".");
            BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
            FileOutputStream fileOS = new FileOutputStream(file);
            byte[] data = new byte[1024];
            int byteContent;
            while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                fileOS.write(data, 0, byteContent);
            }
            fileOS.close();
        } catch (Exception e) {
            Main.getLogger().info("Failed to download file \"" + urlStr + "\":");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Credit to: https://howtodoinjava.com/java/io/how-to-generate-sha-or-md5-file-checksum-hash-in-java/
     * @param digest
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Extracts a zip file to a given directory.
     * Modified code from https://www.journaldev.com/960/java-unzip-file-example
     * @param zipFilePath
     * @param destDir
     */
    public static void extractZip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        //buffer for read and write data to file
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(dir, entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    Files.copy(in, entryDestination.toPath(), REPLACE_EXISTING);
                    in.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Merges A number of ZIP files and puts them in the specified output file.
     * @param destFile
     * @param zipsToMerge
     */
    public static void mergeZips(File destFile, ArrayList<File> zipsToMerge) throws IOException {
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        ZipOutputStream destZip = new ZipOutputStream(new FileOutputStream(destFile));

        ArrayList<String> copiedFiles = new ArrayList<>();

        for (File zipToMerge : zipsToMerge) {
            ZipFile zipFileToMerge = new ZipFile(zipToMerge);
            Enumeration<? extends ZipEntry> entries = zipFileToMerge.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!copiedFiles.contains(entry.getName()) && !entry.isDirectory()) {
                    ZipEntry blankEntry = new ZipEntry(entry.getName());
                    destZip.putNextEntry(blankEntry);
                    InputStream is = zipFileToMerge.getInputStream(entry);
                    byte[] buf = new byte[1024];
                    int len = 0;
                    while ((len = (is.read(buf))) > 0) {
                        destZip.write(buf, 0, Math.min(len, buf.length));
                        //destZip.write(buf);
                    }
                    copiedFiles.add(entry.getName());
                }
            }
            zipFileToMerge.close();
        }
        destZip.close();
    }

    public static void copyRecursive(Path source, Path target, CopyOption... options) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                copy(file, target.resolve(source.relativize(file)), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void copy(Path source, Path dest, CopyOption... options) throws IOException {
        try {
            Files.copy(source, dest, options);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public static void delete(File file) {
        if (file.isDirectory()) {
            String[] files = Objects.requireNonNull(file.list());
            if (files.length == 0) {
                file.delete();
            } else {
                for (String temp : files) {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }
                if (Objects.requireNonNull(file.list()).length == 0) {
                    file.delete();
                }
            }

        } else {
            file.delete();
        }
    }

    public static void copyResourcesRecursively(URL originUrl, File destination) throws Exception {
        URLConnection urlConnection = originUrl.openConnection();
        if (urlConnection instanceof JarURLConnection) {
            copyJarResourcesRecursively((JarURLConnection) urlConnection, destination);
        } else if (urlConnection instanceof FileURLConnection) {
            Files.copy(new File(originUrl.getPath()).toPath(), destination.toPath());
        } else {
            throw new Exception("URLConnection[" + urlConnection.getClass().getSimpleName() +
                    "] is not a recognized/implemented connection type.");
        }
    }

    public static void copyJarResourcesRecursively(JarURLConnection jarConnection, File destination) throws IOException {
        JarFile jarFile = jarConnection.getJarFile();
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith(jarConnection.getEntryName() + "/")) {
                String fileName = entry.getName().substring(jarConnection.getEntryName().length());
                if (!entry.isDirectory()) {
                    try (InputStream entryInputStream = jarFile.getInputStream(entry)) {
                        Files.copy(entryInputStream, Paths.get(destination.getAbsolutePath(), fileName));
                    }
                } else {
                    (new File(destination, fileName)).mkdirs();
                }
            }
        }
    }

}