package net.glasslauncher.legacy.util;

import net.glasslauncher.legacy.Main;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtils {

    public static String readFile(String path) throws IOException {
        return readFile(path, false);
    }

    public static String readFile(String path, boolean isJar)
            throws IOException {
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
        File path;
        File file;
        try {
            path = new File(pathStr);
            file = new File(pathStr + "/" + filename);
            if (md5 != null && file.exists() && getFileChecksum(MessageDigest.getInstance("MD5"), file).toLowerCase().equals(md5.toLowerCase())) {
                return true;
            }
            path.mkdirs();
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
                    Files.copy(in, entryDestination.toPath());
                    in.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
}