package net.glasslauncher.legacy.util;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import javafx.application.*;
import javafx.collections.*;
import javafx.embed.swing.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.web.*;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.jsontemplate.LoginInfo;
import net.glasslauncher.legacy.jsontemplate.MCProfile;
import net.glasslauncher.legacy.jsontemplate.MSAccessToken;
import net.glasslauncher.legacy.jsontemplate.XBResponse;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.logging.Logger;

/**
 * Mostly shamelessly copy-pasted from MineOnline cause I know nothing about MS auth stuff.
 * https://github.com/codieradical/MineOnline/blob/main/src/gg/codie/mineonline/gui/MicrosoftLoginController.java
 */
public class MSLoginHandler {

    private static final String loginUrl = "https://login.live.com/oauth20_authorize.srf" +
            "?client_id=00000000402b5328" +
            "&response_type=code" +
            "&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL" +
            "&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf";

    private static final String redirectUrlSuffix = "https://login.live.com/oauth20_desktop.srf?code=";

    private static final String authTokenUrl = "https://login.live.com/oauth20_token.srf";

    private static final String xblAuthUrl = "https://user.auth.xboxlive.com/user/authenticate";

    private static final String xstsAuthUrl = "https://xsts.auth.xboxlive.com/xsts/authorize";

    private static final String mcLoginUrl = "https://api.minecraftservices.com/authentication/login_with_xbox";

    private static final String mcStoreUrl = "https://api.minecraftservices.com/entitlements/mcstore";

    private static final String mcProfileUrl = "https://api.minecraftservices.com/minecraft/profile";

    private WebView webView;
    private final JDialog frame;
    private final Frame parent;

    private final Gson gson = new Gson();

    public MSLoginHandler(JFrame parent) {
        this.parent = parent;
        frame = new JDialog(parent);
        frame.setModal(true);
        frame.setMinimumSize(new Dimension(500, 600));
        frame.setResizable(false);
    }

    public boolean verifyStoredToken() {
        String token = Config.getLauncherConfig().getLoginInfo().getAccessToken();
        if (token != null && !token.isEmpty() && checkMcProfile(token)) {
            return true;
        }
        Main.LOGGER.warning("Couldn't verify token. Requesting new login.");
        int response = JOptionPane.showConfirmDialog(parent, "Couldn't verify stored MS token. This is normal and just requires you to login again.", "Confirm", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            login();
        }
        return Config.getLauncherConfig().getLoginInfo() != null;
    }

    public void login() {
        JFXPanel panel = new JFXPanel();
        VBox vBox = new VBox();
        Scene scene = new Scene(vBox);
        panel.setScene(scene);
        frame.add(panel);


        Platform.runLater(() -> {
            webView = new WebView();

            java.net.CookieHandler.setDefault(new java.net.CookieManager());

            webView.getEngine().load(loginUrl);
            webView.getEngine().setJavaScriptEnabled(true);
            vBox.getChildren().add(webView);

            // listen to end oauth flow
            AtomicBoolean doThread = new AtomicBoolean(true);
            webView.getEngine().getHistory().getEntries().addListener((ListChangeListener<WebHistory.Entry>) c -> {
                if (c.next() && c.wasAdded()) {
                    for (WebHistory.Entry entry : c.getAddedSubList()) {
                        if (entry.getUrl().startsWith(redirectUrlSuffix)) {
                            String authCode = entry.getUrl().substring(entry.getUrl().indexOf("=") + 1, entry.getUrl().indexOf("&"));
                            doThread.set(false);
                            // once we got the auth code, we can turn it into a oauth token
                            acquireAccessToken(authCode);
                        }
                    }
                }
                if (c.wasAdded() && webView.getEngine().getLocation().contains("oauth20_desktop.srf?error=access_denied")) {
                    frame.dispose();
                    doThread.set(false);
                }
            });
        });
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
    }

    private void acquireAccessToken(String authcode) {
        try {
            URL url = new URL(authTokenUrl);

            Map<Object, Object> data = new HashMap<>();

            data.put("client_id", "00000000402b5328");
            data.put("code", authcode);
            data.put("grant_type", "authorization_code");
            data.put("redirect_uri", "https://login.live.com/oauth20_desktop.srf");
            data.put("scope", "service::user.auth.xboxlive.com::MBI_SSL");

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.getOutputStream().write(ofFormData(data).getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            InputStream is = connection.getInputStream();

            MSAccessToken accessToken = gson.fromJson(new InputStreamReader(is), MSAccessToken.class);
            acquireXBLToken(accessToken.getAccessToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String ofFormData(Map<Object, Object> data) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), "UTF-8"));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
        }
        return builder.toString();
    }

    private void acquireXBLToken(String accessToken) {
        try {
            URL uri = new URL(xblAuthUrl);

            HashMap<String, Object> data = new HashMap<>();
            HashMap<String, Object> properties = new HashMap<>();

            properties.put("AuthMethod", "RPS");
            properties.put("SiteName", "user.auth.xboxlive.com");
            properties.put("RpsTicket", accessToken);

            data.put("Properties", properties);
            data.put("RelyingParty", "http://auth.xboxlive.com");
            data.put("TokenType", "JWT");

            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.getOutputStream().write(gson.toJson(data).getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            InputStream is = connection.getInputStream();

            XBResponse xbAccessToken = gson.fromJson(new InputStreamReader(is), XBResponse.class);
            acquireXsts(xbAccessToken.getAccessToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acquireXsts(String xblToken) {
        try {
            URL uri = new URL(xstsAuthUrl);

            HashMap<String, Object> data = new HashMap<>();
            HashMap<String, Object> properties = new HashMap<>();

            properties.put("SandboxId", "RETAIL");
            properties.put("UserTokens", new String[] { xblToken });

            data.put("Properties", properties);
            data.put("RelyingParty", "rp://api.minecraftservices.com/");
            data.put("TokenType", "JWT");


            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.getOutputStream().write(gson.toJson(data).getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            try {
                InputStream is = connection.getInputStream();

                XBResponse jsonObject = gson.fromJson(new InputStreamReader(is), XBResponse.class);
                String xblXsts = jsonObject.getAccessToken();
                HashMap<String, Object> claims = jsonObject.getDisplayClaims();
                @SuppressWarnings("unchecked") // oh no
                ArrayList<LinkedTreeMap<String, String>> xui = (ArrayList<LinkedTreeMap<String, String>>) claims.get("xui");
                String uhs = (xui.get(0)).get("uhs");
                acquireMinecraftToken(uhs, xblXsts);
            } catch (IOException e) {
                e.printStackTrace();
                InputStream is = connection.getErrorStream();

                XBResponse jsonObject = gson.fromJson(new InputStreamReader(is), XBResponse.class);
                if (jsonObject.getXErr() != null) {
                    long errorCode = jsonObject.getXErr();
                    if (errorCode ==  2148916233L) {
                        JOptionPane.showMessageDialog(null, "This Microsoft account is not signed up with Xbox.\nPlease login to minecraft.net to continue.");
                        frame.dispose();
                    } else if (errorCode == 2148916238L) {
                        if (jsonObject.getRedirect() != null) {
                            webView.getEngine().load(jsonObject.getRedirect());
                        } else {
                            JOptionPane.showMessageDialog(null, "The Microsoft account holder is under 18.\nPlease add this account to a family to continue.");
                            frame.dispose();
                        }
                    }
                } else
                    throw e;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acquireMinecraftToken(String xblUhs, String xblXsts) {
        try {
            URL uri = new URL(mcLoginUrl);

            HashMap<String, Object> data = new HashMap<>();
            data.put("identityToken", "XBL3.0 x=" + xblUhs + ";" + xblXsts);

            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.getOutputStream().write(gson.toJson(data).getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            InputStream is = connection.getInputStream();

            MSAccessToken jsonObject = gson.fromJson(new InputStreamReader(is), MSAccessToken.class);
            String mcAccessToken = jsonObject.getAccessToken();
            //checkMcStore(mcAccessToken);
            checkMcProfile(mcAccessToken);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

// useless?
//    private void checkMcStore(String mcAccessToken) {
//        try {
//            URL uri = new URL(mcStoreUrl);
//
//            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();
//            connection.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
//            connection.setRequestMethod("GET");
//            connection.setDoInput(true);
//
//            InputStream is = connection.getInputStream();
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//            StringBuilder response = new StringBuilder();
//            String line;
//            while ((line = rd.readLine()) != null) {
//                response.append(line);
//                response.append('\r');
//            }
//            rd.close();
//
//            String body = response.toString();
//            System.out.println("store: " + body);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
    private boolean checkMcProfile(String mcAccessToken) {
        try {
            URL uri = new URL(mcProfileUrl);

            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            if(connection.getResponseCode() == 404) {
                JOptionPane.showMessageDialog(null, "This Microsoft account does not own Minecraft.");
                frame.dispose();
                return false;
            }

            InputStream is = connection.getInputStream();

            MCProfile jsonObject = gson.fromJson(new InputStreamReader(is), MCProfile.class);
            String name = jsonObject.getName();

            frame.dispose();
            Config.getLauncherConfig().setMSToken(true);
            Config.getLauncherConfig().setLoginInfo(new LoginInfo(name, mcAccessToken), true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Config.getLauncherConfig().setMSToken(false);
        Config.getLauncherConfig().setLoginInfo(null);
        return false;
    }
}
