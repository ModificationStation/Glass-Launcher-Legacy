package net.glasslauncher.legacy.util;

import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.FileUtils;
import net.glasslauncher.legacy.Main;

import java.io.*;
import java.security.*;

public class TempImageHandler {

    public static String getImage(String img) throws IOException, NoSuchAlgorithmException {
        Main.LOGGER.info("Downloading image " + img + " to temp...");
        String imageChecksum = getStringChecksum(MessageDigest.getInstance("MD5"), img);

        if (!FileUtils.downloadFile(img, CommonConfig.getGlassPath() + "cache/repo-images", null, imageChecksum)) {
            throw new IOException("Failed to download image.");
        }
        return imageChecksum;
    }

    private static String getStringChecksum(MessageDigest digest, String str) throws IOException {
        InputStream fis = new ByteArrayInputStream(str.getBytes());
        byte[] byteArray = new byte[1024];

        int bytesCount;
        while((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        fis.close();
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        byte[] var7 = bytes;
        int var8 = bytes.length;

        for(int var9 = 0; var9 < var8; ++var9) {
            byte b = var7[var9];
            sb.append(Integer.toString((b & 255) + 256, 16).substring(1));
        }

        return sb.toString();
    }
}
