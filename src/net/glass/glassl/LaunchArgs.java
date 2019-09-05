package net.glass.glassl;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import javax.swing.*;
import javax.xml.ws.http.HTTPException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static net.glass.glassl.Main.logger;

public class LaunchArgs {
    private String instance;
    private String instpath;
    private HttpURLConnection req;

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
            logger.severe("Got " + args.length + " args, expected 3.");
            return null;
        }
        instance = args[2];
        instpath = Config.instpath + "instances/" + instance;
        String version = getVersion();
        String javaargs = getJavaArgs();
        String[] logininfo;
        if (args[1].isEmpty()) {
            logininfo = new String[] {"", args[0]};
        }
        else {
            logininfo = login(args[0], args[1]);
        }

        if (logininfo == null) {
            logger.severe("Aborting launch.");
            return null;
        }

        String session = logininfo[0];
        String username = logininfo[1];

        return new String[] {
                username,
                session,
                version,
                "true",
                instance
        };
    }

    public String[] login(String username, String password) {
        try {
            req.setRequestMethod("POST");
            req.setRequestProperty("Content-Type", "application/json");
            req.setDoOutput(true);
            req.setDoInput(true);
            OutputStreamWriter wr = new OutputStreamWriter(req.getOutputStream());
            JsonObject creds = new JsonObject();
            creds.put("username", username);
            creds.put("password", password);
            JsonObject agent = new JsonObject();
            agent.put("name", "Minecraft");
            agent.put("version", 1);
            creds.put("agent", agent);
            wr.write(JsonWriter.objectToJson(creds));
            wr.flush();

            BufferedReader res = new BufferedReader(new InputStreamReader(req.getInputStream()));
            StringBuilder resj = new StringBuilder();
            for (String strline = ""; strline != null; strline = res.readLine()) {
                resj.append(strline);
            }
            JsonObject session = (JsonObject) JsonReader.jsonToJava(resj.toString());

            if (req.getResponseCode() != 200) {
                logger.severe("Error sending request!");
                logger.severe("Code: " + req.getResponseCode());
                logger.severe("Error: " + session.get("error"));
                throw new HTTPException(req.getResponseCode());
            }
            JsonObject profile = (JsonObject) session.get("selectedProfile");
            return new String[] {(String) session.get("accessToken"), (String) profile.get("name")};
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
                JOptionPane.showMessageDialog(null, "Server responded with \"HTTP " + req.getResponseCode() + "\". Make sure your username and password are correct and try again.");
            }
            catch (IOException ex) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,"Login failed! Make sure you have an internet connection!");
            }
            return null;
        }
    }

    public String getVersion() {
        String version;
        File jsonfile = new File(instpath + "/.minecraft/modpack.json");
        try {
            JsonObject instjson = (JsonObject) JsonReader.jsonToJava(new Scanner(new FileInputStream(jsonfile), "UTF-8").useDelimiter("\\A").next());
            //instjson = (JSONObject) instjson.get("modpack");
            version = (String) instjson.get("mcver");
        }
        catch (Exception e) {
            logger.severe("No instance config found!");
            e.printStackTrace();
            return "b1.7.3";
        }
        return version;
    }

    public String getJavaArgs() {
        File jsonfile = new File(instpath + "/instance_config.json");
        String javaargs;

        try {
            JsonObject instjson = (JsonObject) JsonReader.jsonToJava(new Scanner(new FileInputStream(jsonfile), "UTF-8").useDelimiter("\\A").next());
            javaargs = "-Xmx" + instjson.get("maxram") + " -Xms" + instjson.get("minram") + " " + instjson.get("javaargs");
        }
        catch (Exception e) {
            logger.severe("No instance config found!");
            e.printStackTrace();
            return "";
        }
        return javaargs;
    }
}
