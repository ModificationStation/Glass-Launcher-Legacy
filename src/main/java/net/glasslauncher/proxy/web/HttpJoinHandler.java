package net.glasslauncher.proxy.web;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.jsontemplate.Profile;
import net.glasslauncher.legacy.jsontemplate.ServerJoin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class HttpJoinHandler implements HttpHandler {

	public void handle(HttpExchange t) {
		try {
			// Original: http://www.minecraft.net/game/joinserver.jsp?user=calmilamsy&sessionId=-&serverId=77e59ac042d8a50e
			// New:      http://localhost:25561/join/?user=calmilamsy&sessionId=-&serverId=77e59ac042d8a50e

            // req = /join/?user=calmilamsy&sessionId=-&serverId=77e59ac042d8a50e
            String req = t.getRequestURI().toString();
            String[] reqParts = req.split("[&=]");

            // SERVER
			if (reqParts.length == 4) {
				String response;
				// Turns to: https://sessionserver.mojang.com/session/minecraft/hasJoined?username=calmilamsy&serverId=77e59ac042d8a50e
				req = req.replaceFirst("user", "username");
				req = req.replaceFirst("/join/", "/session/minecraft/hasJoined");
				req = "https://sessionserver.mojang.com" + req;
				
				HttpURLConnection reqJoined = (HttpURLConnection) (new URL(req)).openConnection();
				if (reqJoined.getResponseCode() == 200) {
					response = "YES";
				} else {
					response = "NO";
				}
				
				t.sendResponseHeaders(200, response.getBytes().length);
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();
			// CLIENT
			} else {
				reqParts = new String[] {reqParts[1], reqParts[3], reqParts[5]};
				URL nameURL = new URL("https://api.mojang.com/users/profiles/minecraft/" + reqParts[0]);
				URLConnection nameUrlConnection = nameURL.openConnection();
				String response = convertStreamToString(nameUrlConnection.getInputStream());

				Profile profile = (new Gson()).fromJson(response, Profile.class);
				String uuid = profile.getId();

				ServerJoin serverJoin = new ServerJoin();
				serverJoin.setAccessToken(reqParts[1]);
				serverJoin.setSelectedProfile(uuid);
				serverJoin.setServerId(reqParts[2]);


				HttpURLConnection reqJoin = (HttpURLConnection) (new URL("https://sessionserver.mojang.com/session/minecraft/join")).openConnection();
				reqJoin.setRequestMethod("POST");
				reqJoin.setRequestProperty("Content-Type", "application/json");
				reqJoin.setDoOutput(true);

				OutputStreamWriter wr = new OutputStreamWriter(reqJoin.getOutputStream());
				wr.write((new Gson()).toJson(serverJoin));
				wr.flush();

				if (reqJoin.getResponseCode() != 204) {
					throw new IOException("Got unexpeced response from join request: " + reqJoin.getResponseCode());
				} else {
					response = "ok";
				}
				t.sendResponseHeaders(200, response.getBytes().length);

				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();

			}
			t.close();
		} catch (Exception e) {
            Main.LOGGER.severe("Exception while handling join:");
            try {
                t.sendResponseHeaders(500, 0);
            } catch (Exception ignored) {}
			e.printStackTrace();
			t.close();
		}
		
	}

	static String convertStreamToString(InputStream is) {
	    Scanner s = new Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}