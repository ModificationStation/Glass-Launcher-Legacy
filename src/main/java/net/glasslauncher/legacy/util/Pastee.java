package net.glasslauncher.legacy.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.glasslauncher.jsontemplate.PasteePost;
import net.glasslauncher.jsontemplate.PasteePostSection;
import net.glasslauncher.jsontemplate.PasteeResponse;
import net.glasslauncher.legacy.Main;

import javax.xml.ws.http.HTTPException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Pastee {
    private HttpURLConnection req;
    private String text;

    public Pastee(String text) {
        try {
            this.req = (HttpURLConnection) new URL("https://api.paste.ee/v1/pastes").openConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        this.text = text;
    }

    public String post() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return post("Cactus Juice Log at " + dateFormat.format(date));
    }

    public String post(String name) {
        try {
            Gson gson = new GsonBuilder().create();
            req.setRequestMethod("POST");
            req.setRequestProperty("Content-Type", "application/json");
            req.setRequestProperty("X-Auth-Token", "ak4XFTvAbNJvaEIoycGzOhCYeLkd7JFpZLVtUgutM");
            req.setDoOutput(true);
            req.setDoInput(true);
            PasteePost pasteePost = new PasteePost();
            pasteePost.setDescription(name);
            PasteePostSection[] pasteePostSections = new PasteePostSection[1];
            pasteePostSections[0] = new PasteePostSection();
            pasteePostSections[0].setContents(text);
            pasteePost.setSections(pasteePostSections);

            OutputStreamWriter wr = new OutputStreamWriter(req.getOutputStream());
            wr.write(gson.toJson(pasteePost));
            wr.flush();

            BufferedReader res = new BufferedReader(new InputStreamReader(req.getInputStream()));
            StringBuilder resj = new StringBuilder();
            for (String strline = ""; strline != null; strline = res.readLine()) {
                resj.append(strline);
            }
            PasteeResponse resp = gson.fromJson(resj.toString(), PasteeResponse.class);

            if (req.getResponseCode() != 201) {
                Main.getLogger().severe("Error sending request!");
                Main.getLogger().severe("Code: " + req.getResponseCode());
                throw new HTTPException(req.getResponseCode());
            }
            Main.getLogger().info(resj.toString());
            return resp.getLink();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
