package net.glasslauncher.proxy.web;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.glasslauncher.jsontemplate.Profile;
import net.glasslauncher.jsontemplate.ProfileProperties;
import net.glasslauncher.jsontemplate.TextureURLs;
import net.glasslauncher.jsontemplate.Textures;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class HttpSkinHandler implements HttpHandler {
    private int isCape;

    public HttpSkinHandler(int isCape) throws IllegalArgumentException {
        if (isCape >1 || isCape <0) {
            throw new IllegalArgumentException("Value must be 0 or 1.");
        }
        this.isCape = isCape;
    }

    public void handle(HttpExchange t) throws IOException {
        try {
            String path = t.getRequestURI().toString();
            String username = path.substring(path.lastIndexOf('/') + 1);

            if (username.toLowerCase().endsWith(".png")) {
                username = username.substring(0, username.lastIndexOf(".png"));
            }

            BufferedImage texture;
            try {
                texture = getCapeSkin(username, isCape);
            } catch (Exception e) {
                if (e.getMessage().contains("429")) {
                    t.sendResponseHeaders(429, 0);
                } else {
                    t.sendResponseHeaders(500, 0);
                }
                t.close();
                return;
            }

            if (texture == null) {
                t.sendResponseHeaders(404, 0);
                t.close();
                return;
            }

            byte[] response = imageToBytes(texture);

            Headers headers = t.getResponseHeaders();
            headers.set("Content-Type", "image/png");
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        } catch (Exception e) {
        	System.out.println("glass-netfix: Failed to handle skin:");
            e.printStackTrace();
            t.sendResponseHeaders(500, 0);
            t.close();
        }
    }

    public static BufferedImage[] getImages(String username) throws IOException {
        String uuid = WebUtils.getUUID(username);
        if (uuid == null) {
            return null;
        }
        Profile profile = (new Gson()).fromJson(WebUtils.getJsonFromURL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid), Profile.class);

        // It doesnt work if I just try casting the object. Don't ask why.
        ProfileProperties properties = profile.getProperties()[0];

        byte[] base64 = (properties.getValue()).getBytes(StandardCharsets.UTF_8);
        Textures textures = (new Gson()).fromJson(new String(Base64.getDecoder().decode(base64)), Textures.class);

        TextureURLs textureURLs = textures.getTextures();
        Map skin = textureURLs.getSKIN();
        Map cape = textureURLs.getCAPE();

        BufferedImage[] images = new BufferedImage[2];

        try {
            File skinCache = WebUtils.checkCache(username + "/skin.png");
            URL skinUrl = new URL((String) skin.get("url"));
            if (skinCache != null) {
                skinUrl = skinCache.toURI().toURL();
            }
            images[0] = ImageIO.read(skinUrl);

        } catch (Exception e) {
            images[0] = null;
        }

        try {
            File capeCache = WebUtils.checkCache(username + "/cape.png");
            URL capeUrl = new URL((String) cape.get("url"));
            if (capeCache != null) {
                capeUrl = capeCache.toURI().toURL();
            }
            images[1] = ImageIO.read(capeUrl);
        } catch (Exception e) {
            images[1] = null;
        }
        return images;
    }

    /**
     * Checks to see if skin needs updating and updates if necessary.
     *
     * @param username Username to check.
     * @param isCape
     */
    public static BufferedImage getCapeSkin(String username, int isCape) throws IOException {
        String[] image = new String[] {"/skin.png", "/cape.png"};
        WebUtils.makeCacheFolders(username);
        File metaFile = WebUtils.checkCache(username + "/meta");
        if (metaFile == null) {
            try {
                BufferedImage[] images = getImages(username);

                if (images == null) {
                    return null;
                }
                if (images[0] != null) {
                    ImageIO.write(images[0], "png", WebUtils.getCache(username + "/skin.png"));
                }
                if (images[1] != null) {
                    ImageIO.write(images[1], "png", WebUtils.getCache(username + "/cape.png"));
                }
                WebUtils.putCache(WebUtils.getCache(username + "/meta"), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File imageFile = WebUtils.getCache(username + image[isCape]);
        BufferedImage bufferedImage;
        if (imageFile.exists()) {
            bufferedImage = ImageIO.read(imageFile);
        } else {
            bufferedImage = null;
        }
        return bufferedImage;
    }

    public static byte[] imageToBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", imageBytes);
        imageBytes.flush();
        byte[] response = imageBytes.toByteArray();
        imageBytes.close();
        return response;
    }
}
