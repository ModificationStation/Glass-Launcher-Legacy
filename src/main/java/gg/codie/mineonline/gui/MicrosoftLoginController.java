package gg.codie.mineonline.gui;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.MSLoginWindow;
import net.glasslauncher.legacy.jsontemplate.LoginInfo;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;


/**
 * Mostly shamelessly copy-pasted from MineOnline cause I know nothing about MS auth stuff.
 * <a href="https://github.com/craftycodie/MineOnline/blob/main/src/main/java/gg/codie/mineonline/gui/MicrosoftLoginController.java">Link to Original File</a>
 */
public class MicrosoftLoginController {
    private static final String xblAuthUrl = "https://user.auth.xboxlive.com/user/authenticate";

    private static final String xstsAuthUrl = "https://xsts.auth.xboxlive.com/xsts/authorize";

    private static final String mcLoginUrl = "https://api.minecraftservices.com/authentication/login_with_xbox";

    private static final String mcStoreUrl = "https://api.minecraftservices.com/entitlements/mcstore";

    private static final String mcProfileUrl = "https://api.minecraftservices.com/minecraft/profile";

    private static final String deviceCodeUrl = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";

    private static final String loginPollUrl = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";

    private static final String clientId = "289b8586-ce71-4e43-aed0-d7a05f5f257d";

    private static final MicrosoftLoginController singleton = new MicrosoftLoginController();

    public static void loadDeviceCode(MSLoginWindow loginWindow) {
        singleton.reset(loginWindow);
        singleton.deviceCode();
    }

    String userCode;
    String verificationUrl;
    String deviceCode;
    String error;

    boolean isLoggingIn;

    Thread loginPollThread;

    private void reset(MSLoginWindow loginWindow) {
        deviceCode = null;
        userCode = null;
        verificationUrl = null;
        error = null;
        isLoggingIn = false;
        loginPollThread = new Thread(){
            public void run(){
            while (isLoggingIn && error == null) {
                deviceCodeLoginPoll();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(error != null) {
                loginWindow.setErrorText(error);
            }
            loginWindow.getContinueButton().setEnabled(true);
            interrupt();
            }
        };
    }

    public static String getLoginCode() {
        return singleton.userCode;
    }

    public static String getVerificationUrl() {
        return singleton.verificationUrl;
    }

    public static String getError() {
        return singleton.error;
    }

    private void deviceCode() {
        try {
            URL url = new URL(deviceCodeUrl);

            Map<Object, Object> data = new HashMap<>();

            data.put("client_id", clientId);
            data.put("scope", "XboxLive.signin offline_access");

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.getOutputStream().write(ofFormData(data).getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            InputStream is = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            if (connection.getResponseCode() >= 400) {
                System.out.println(response);
                error = "Failed to retrieve login code!";
                return;
            }

            JsonObject jsonObject = Jankson.builder().build().load(response.toString());

            userCode = jsonObject.get(String.class, "user_code");
            verificationUrl = jsonObject.get(String.class, "verification_uri");
            deviceCode = jsonObject.get(String.class, "device_code");

            isLoggingIn = true;
            loginPollThread.start();
        } catch (UnknownHostException e) {
            error = "Failed to contact Microsoft. Are you offline?";
            isLoggingIn = false;
            e.printStackTrace();
        } catch (Exception e) {
            error = "Something went wrong!";
            isLoggingIn = false;
            e.printStackTrace();
        }
    }

    private void deviceCodeLoginPoll() {
        try {
            URL url = new URL(loginPollUrl);

            Map<Object, Object> data = new HashMap<>();

            data.put("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
            data.put("client_id", clientId);
            data.put("device_code", deviceCode);

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.getOutputStream().write(ofFormData(data).getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            InputStream is = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            JsonObject jsonObject = Jankson.builder().build().load(response.toString());

            String errorCode = jsonObject.get(String.class, "error");

            if(errorCode == null) {
                errorCode = "";
            }

            if (errorCode.equals("authorization_pending")) {
                return;
            }

            if (errorCode.equals("authorization_declined")) {
                error = "Login was cancelled.";
                isLoggingIn = false;
                return;
            }

            if (errorCode.equals("bad_verification_code")) {
                error = "Something went wrong!";
                isLoggingIn = false;
                return;
            }

            if (errorCode.equals("expired_token")) {
                error = "Login has expired.";
                isLoggingIn = false;
                return;
            }

            if (connection.getResponseCode() != 200) {
                error = "Something went wrong!";
                isLoggingIn = false;
                System.out.println(response);
                return;
            }

            singleton.isLoggingIn = false;

            String accessToken = jsonObject.get(String.class, "access_token");
            acquireXBLToken(accessToken);
        } catch (UnknownHostException e) {
            error = "Failed to contact Microsoft. Are you offline?";
            isLoggingIn = false;
            e.printStackTrace();
        } catch (Exception e) {
            error = "Something went wrong!";
            isLoggingIn = false;
            e.printStackTrace();
        }
    }

    private void acquireXBLToken(String accessToken) {
        try {
            URL uri = new URL(xblAuthUrl);

            JsonObject data = new JsonObject();
            JsonObject properties = new JsonObject();

            properties.put("AuthMethod", new JsonPrimitive("RPS"));
            properties.put("SiteName", new JsonPrimitive("user.auth.xboxlive.com"));
            properties.put("RpsTicket", new JsonPrimitive("d=" + accessToken));

            data.put("Properties", properties);
            data.put("RelyingParty", new JsonPrimitive("http://auth.xboxlive.com"));
            data.put("TokenType", new JsonPrimitive("JWT"));

            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.getOutputStream().write(data.toString().getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            InputStream is = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            if (connection.getResponseCode() != 200) {
                error = "Failed to login to Xbox Live";
                isLoggingIn = false;
                System.out.println(response);
                return;
            }

            JsonObject jsonObject = Jankson.builder().build().load(response.toString());
            String xblToken = jsonObject.get(String.class, "Token");
            acquireXsts(xblToken);
        } catch (UnknownHostException e) {
            error = "Failed to contact Xbox Live. Are you offline?";
            isLoggingIn = false;
            e.printStackTrace();
        } catch (Exception e) {
            error = "Something went wrong!";
            isLoggingIn = false;
            e.printStackTrace();
        }
    }

    private void acquireXsts(String xblToken) {
        try {
            URL uri = new URL(xstsAuthUrl);

            JsonObject data = new JsonObject();
            JsonObject properties = new JsonObject();

            properties.put("SandboxId", new JsonPrimitive("RETAIL"));
            JsonArray xblTokenElement = new JsonArray();
            xblTokenElement.add(new JsonPrimitive(xblToken));
            properties.put("UserTokens", xblTokenElement);

            data.put("Properties", properties);
            data.put("RelyingParty", new JsonPrimitive("rp://api.minecraftservices.com/"));
            data.put("TokenType", new JsonPrimitive("JWT"));


            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.getOutputStream().write(data.toString().getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            try {
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();

                JsonObject jsonObject = Jankson.builder().build().load(response.toString());
                String xblXsts = jsonObject.get(String.class, "Token");
                JsonObject claims = (JsonObject) jsonObject.get("DisplayClaims");
                JsonArray xui = claims.get(JsonArray.class, "xui");
                String uhs = xui.get(JsonObject.class, 0).get(String.class, "uhs");
                acquireMinecraftToken(uhs, xblXsts);
            } catch (IOException e) {
                InputStream is = connection.getErrorStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();

                JsonObject jsonObject = Jankson.builder().build().load(response.toString());
                if (jsonObject.containsKey("XErr")) {
                    long errorCode = jsonObject.getLong("XErr", 0);
                    if (errorCode ==  2148916233L) {
                        error = "You are not signed up with Xbox.\nPlease login to minecraft.net to continue.";
                    } else if (errorCode == 2148916238L) {
                        error = "Account holder is under 18.\nPlease add this account to a family to continue.";
                    } else if (errorCode == 2148916235L) {
                        error = "Account is registered in a country where Xbox Live is not available.";
                    } else if (errorCode == 2148916236L) {
                        error = "You must complete Adult verification on the Xbox Live homepage.";
                    } else if (errorCode == 2148916237L) {
                        error = "You must complete Age verification on the Xbox Live homepage.";
                    } else {
                        error = "Failed to login to Xbox Live";
                        System.out.println(response);
                    }
                } else {
                    error = "Failed to login to Xbox Live";
                    System.out.println(response);
                }
            }
        } catch (UnknownHostException e) {
            error = "Failed to contact Xbox Live. Are you offline?";
            isLoggingIn = false;
            e.printStackTrace();
        } catch (Exception e) {
            error = "Something went wrong!";
            isLoggingIn = false;
            e.printStackTrace();
        }
    }

    private void acquireMinecraftToken(String xblUhs, String xblXsts) {
        try {
            URL uri = new URL(mcLoginUrl);

            JsonObject data = new JsonObject();
            data.put("identityToken", new JsonPrimitive("XBL3.0 x=" + xblUhs + ";" + xblXsts));

            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.getOutputStream().write(data.toString().getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            JsonObject jsonObject = Jankson.builder().build().load(response.toString());
            String mcAccessToken = jsonObject.get(String.class, "access_token");
            checkMcStore(mcAccessToken);
            checkMcProfile(mcAccessToken);
        } catch (UnknownHostException e) {
            error = "Failed to contact Minecraft Services. Are you offline?";
            isLoggingIn = false;
            e.printStackTrace();
        } catch (Exception e) {
            error = "Something went wrong!";
            isLoggingIn = false;
            e.printStackTrace();
        }
    }

    private void checkMcStore(String mcAccessToken) {
        try {
            URL uri = new URL(mcStoreUrl);

            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
        } catch (UnknownHostException e) {
            error = "Failed to contact Minecraft Services. Are you offline?";
            isLoggingIn = false;
            e.printStackTrace();
        } catch (Exception e) {
            error = "Something went wrong!";
            isLoggingIn = false;
            e.printStackTrace();
        }
    }

    private void checkMcProfile(String mcAccessToken) {
        try {
            URL uri = new URL(mcProfileUrl);

            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            if(connection.getResponseCode() == 404) {
                JOptionPane.showMessageDialog(null, "This Microsoft account does not own Minecraft.");
            }

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            JsonObject jsonObject = Jankson.builder().build().load(response.toString());
            String name = jsonObject.get(String.class, "name");
            String uuid = jsonObject.get(String.class, "id");


            LoginInfo loginInfo = new LoginInfo(name, mcAccessToken, uuid);
            Config.getLauncherConfig().setLoginInfo(loginInfo);
            reset(null);
        } catch (UnknownHostException e) {
            error = "Failed to contact Minecraft Services. Are you offline?";
            isLoggingIn = false;
            e.printStackTrace();
        } catch (Exception e) {
            error = "Something went wrong!";
            isLoggingIn = false;
            e.printStackTrace();
        }
    }

    public static boolean validateToken(String mcAccessToken) {
        try {
            URL uri = new URL(mcProfileUrl);

            HttpURLConnection connection = (HttpURLConnection)uri.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            return connection.getResponseCode() == 200 || connection.getResponseCode() == 204;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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
}
