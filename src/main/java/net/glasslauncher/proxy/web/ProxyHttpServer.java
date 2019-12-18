package net.glasslauncher.proxy.web;

import com.sun.net.httpserver.HttpServer;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;

import java.net.InetSocketAddress;

public final class ProxyHttpServer {
    public static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(Config.getProxywebport()), 0);
        server.createContext("/skins/", new HttpSkinHandler(0));
        server.createContext("/capes/", new HttpSkinHandler(1));
        //server.createContext("/join/", new HttpJoinHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        Main.getLogger().info("Proxy webserver started!");
    }
}