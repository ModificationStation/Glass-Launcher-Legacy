package net.glasslauncher.proxy.web;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public final class ProxyHttpServer {
    public static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(25561), 0);
        server.createContext("/skins/", new HttpSkinHandler(0));
        server.createContext("/capes/", new HttpSkinHandler(1));
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}