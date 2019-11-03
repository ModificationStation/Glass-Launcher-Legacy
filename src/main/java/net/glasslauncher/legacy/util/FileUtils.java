package net.glasslauncher.legacy.util;

import net.glasslauncher.legacy.Main;
import proxy.web.WebUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Scanner;

public class FileUtils {

    public static String readFile(String path) throws IOException, URISyntaxException {
        return readFile(path, false);
    }

    public static String readFile(String path, boolean isJar)
            throws IOException, URISyntaxException {
        byte[] encoded;
        if (isJar) {
            String resLocation = path.split("(!)(?!.*\1)")[1].replaceAll("\\\\", "/");
            encoded = convertStreamToString(Main.class.getResourceAsStream(resLocation)).getBytes(StandardCharsets.UTF_8);
        } else {
            encoded = Files.readAllBytes(Paths.get(path.replaceAll("\\\\", "/")));
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static boolean downloadFile(String urlStr, String path) {
        return downloadFile(urlStr, path, null);
    }

    public static boolean downloadFile(String urlStr, String path, String md5) {
        String filename = urlStr.substring(urlStr.lastIndexOf('/') + 1);
        return downloadFile(urlStr, path, md5, filename);
    }

    /**
     * Downloads given URL to target path.
     * @param urlStr File to download.
     * @param pathStr Path to save the file to (filename decided by URL).
     * @param md5 MD5 to compare against. Ignored if null.
     * @return false if no file was downloaded, true if otherwise.
     */
    public static boolean downloadFile(String urlStr, String pathStr, String md5, String filename) {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (Exception e) {
            Main.logger.info("Failed to download file \"" + urlStr + "\": Invalid URL.");
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
            Main.logger.info("Failed to download file \"" + urlStr + "\": Invalid path.");
            e.printStackTrace();
            return false;
        }

        try {
            Main.logger.info("Downloading \"" + urlStr + "\".");
            BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
            FileOutputStream fileOS = new FileOutputStream(file);
            byte[] data = new byte[1024];
            int byteContent;
            while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                fileOS.write(data, 0, byteContent);
            }
            fileOS.close();
        } catch (Exception e) {
            Main.logger.info("Failed to download file \"" + urlStr + "\":");
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
    private static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    private static void makeDirs(String path) {
        try {
            (new File(path)).mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
