package net.glasslauncher.proxy.web;
/*
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Scanner;

import javax.xml.ws.http.HTTPException;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import glassproxy.Config;

public class HttpJoinHandler implements HttpHandler {

	public void handle(HttpExchange t) throws IOException {
		try {
			String req = t.getRequestURI().toString();
			// Original: http://www.minecraft.net/game/joinserver.jsp?user=calmilamsy&sessionId=-&serverId=77e59ac042d8a50e
			// New:      http://localhost:25561/join/?user=calmilamsy&sessionId=-&serverId=77e59ac042d8a50e
			if (Config.isServer()) {
				String response;
				// Turns to: https://sessionserver.mojang.com/session/minecraft/hasJoined?username=calmilamsy&serverId=77e59ac042d8a50e
				req = req.replaceFirst("user", "username");
				req = req.replaceFirst("/join/", "/session/minecraft/hasJoined");
				req = "https://sessionserver.mojang.com" + req;
				
				System.out.println(req);
				
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
			} else {
				String[] reqParts = req.split("[&=]");
				reqParts = new String[] {reqParts[1], reqParts[3], reqParts[5]};
				URL nameURL = new URL("https://api.mojang.com/users/profiles/minecraft/" + reqParts[0]);
				URLConnection nameUrlConnection = nameURL.openConnection();
				String response = convertStreamToString(nameUrlConnection.getInputStream());

				JsonObject responseJson = (JsonObject) JsonReader.jsonToJava(response);
				String uuid = (String) responseJson.get("id");

				JsonObject reqJson = new JsonObject();

				reqJson.put("accessToken", reqParts[1]);
				reqJson.put("selectedProfile", uuid);
				reqJson.put("serverId", reqParts[2]);


				HttpURLConnection reqJoin = (HttpURLConnection) (new URL("https://sessionserver.mojang.com/session/minecraft/join")).openConnection();
				reqJoin.setRequestMethod("POST");
				reqJoin.setRequestProperty("Content-Type", "application/json");
				reqJoin.setDoOutput(true);

				OutputStreamWriter wr = new OutputStreamWriter(reqJoin.getOutputStream());
				System.out.println(JsonWriter.objectToJson(reqJson, Config.prettyprint));
				wr.write(JsonWriter.objectToJson(reqJson, Config.prettyprint));
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
			System.err.println("glass-netfix: Exception while handling join:");
			e.printStackTrace();
			try {
				t.sendResponseHeaders(500, 0);
			} catch (Exception ignored) {
			}
			t.close();
		}
		
	}
	
	static String convertStreamToString(InputStream is) {
	    Scanner s = new Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}*/