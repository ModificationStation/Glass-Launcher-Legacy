package net.glasslauncher.legacy.mc;

import com.google.gson.Gson;
import net.glasslauncher.jsontemplate.*;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;

import javax.swing.*;
import javax.xml.ws.http.HTTPException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class LaunchArgs {
    private String instpath;
    private HttpURLConnection req;
    private InstanceConfig instjson;

    {
        try {
            req = (HttpURLConnection) new URL("https://authserver.mojang.com/authenticate").openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getArgs(String[] args) {
        // 0: username, 1: pass, 2: instance
        if (args.length != 3) {
            Main.getLogger().severe("Got " + args.length + " args, expected 3.");
            return null;
        }
        String instance = args[2];
        instpath = Config.getGlassPath() + "instances/" + instance;
        try {
            instjson = (new Gson()).fromJson(new FileReader(instpath + "/instance_config.json"), InstanceConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        String version = getVersion();
        String javaargs = getJavaArgs();
        String[] logininfo;
        if (args[1].isEmpty()) {
            logininfo = new String[]{"", args[0]};
        } else {
            logininfo = login(args[0], args[1]);
        }

        if (logininfo == null) {
            Main.getLogger().severe("Aborting launch.");
            return null;
        }

        String session = logininfo[0];
        String username = logininfo[1];

        return new String[]{
                username,
                session,
                version,
                getProxyArg(),
                instance
        };
    }

    public String[] login(String username, String password) {
        Gson gson = new Gson();
        try {
            req.setRequestMethod("POST");
            req.setRequestProperty("Content-Type", "application/json");
            req.setDoOutput(true);
            req.setDoInput(true);
            OutputStreamWriter wr = new OutputStreamWriter(req.getOutputStream());
            LoginCreds creds = new LoginCreds();
            creds.setUsername(username);
            creds.setPassword(password);
            wr.write(gson.toJson(creds));
            wr.flush();

            BufferedReader res = new BufferedReader(new InputStreamReader(req.getInputStream()));
            StringBuilder resj = new StringBuilder();
            for (String strline = ""; strline != null; strline = res.readLine()) {
                resj.append(strline);
            }
            LoginResponse session = gson.fromJson(resj.toString(), LoginResponse.class);

            if (req.getResponseCode() != 200) {
                Main.getLogger().severe("Error sending request!");
                Main.getLogger().severe("Code: " + req.getResponseCode());
                Main.getLogger().severe("Error: " + session.getError());
                throw new HTTPException(req.getResponseCode());
            }
            LoginResponseAgent profile = session.getSelectedProfile();
            return new String[]{session.getAccessToken(), profile.getName()};
        } catch (Exception e) {
            e.printStackTrace();
            try {
                JOptionPane.showMessageDialog(null, "Server responded with \"HTTP " + req.getResponseCode() + "\". Make sure your username and password are correct and try again.");
            } catch (IOException ex) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Login failed! Make sure you have an internet connection!");
            }
            return null;
        }
    }

    private String getVersion() {
        return instjson.getVersion();
    }

    private String getJavaArgs() {
        String javaargs = "";

        if (instjson.getMaxRam() != null) {
            javaargs += "-Xmx" + instjson.getMaxRam();
        } else {
            javaargs += "-Xmx512m";
        }
        if (instjson.getMinRam() != null) {
            javaargs += "-Xms" + instjson.getMinRam();
        } else {
            javaargs += "-Xms64m";
        }
        if (instjson.getJavaArgs() != null) {
            javaargs += instjson.getJavaArgs();
        }

        return javaargs;
    }

    private String getProxyArg() {
        return String.valueOf(instjson.isProxySound() || instjson.isProxyCape() || instjson.isProxySkin() || instjson.isProxyLogin());
    }
}
