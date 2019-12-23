package net.glasslauncher.proxy.web;

import com.sun.net.httpserver.HttpServer;
import net.glasslauncher.legacy.Config;

import java.io.IOException;
import java.net.InetSocketAddress;

public final class ProxyHttpServer {
    public static HttpServer start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(Config.PROXY_WEB_PORT), 0);
        server.createContext("/skins/", new HttpSkinHandler(0));
        server.createContext("/capes/", new HttpSkinHandler(1));
        server.createContext("/join/", new HttpJoinHandler());
        server.setExecutor(null); // creates a default executor
        return server;
    }
}