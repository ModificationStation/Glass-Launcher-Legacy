package net.glasslauncher.proxy;

import com.sun.net.httpserver.HttpServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import net.glasslauncher.proxy.web.ProxyHttpServer;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.mitm.Authority;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;

import java.io.File;

public class Proxy extends Thread {
    private HttpProxyServerBootstrap serverBoot;
    private HttpProxyServer httpProxyServer;
    private HttpServer httpServer;

    public Proxy(boolean[] args) {
        try {
            // 1: args[sound, skin, cape]

            httpServer = ProxyHttpServer.start();

            this.serverBoot =
                    DefaultHttpProxyServer.bootstrap()
                            .withPort(Config.getPROXY_PORT())
                            .withManInTheMiddle(new CertificateSniffingMitmManager(new Authority(
                                    new File(Config.getCACHE_PATH()),
                                    "glass-launcher-proxy-mitm",
                                    "thisisranlocallysothisdoesntmatter".toCharArray(),
                                    "Glass Launcher",
                                    "Glass Launcher",
                                    "glass-launcher-proxy, a simple proxy to fix legacy MC.",
                                    "Glass Launcher",
                                    "glass-launcher-proxy, used by Glass-launcher to fix sounds, skins and capes in legacy Minecraft."
                            )))
                            .withFiltersSource(new HttpFiltersSourceAdapter() {
                                public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                                    ProxyFilter proxyFilter = new ProxyFilter(originalRequest);
                                    proxyFilter.setArgs(args);
                                    return proxyFilter;
                                }
                            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        httpProxyServer = serverBoot.start();
        httpServer.start();
        Main.getLogger().info("Proxy servers started!");
    }

    public void exit() {
        httpProxyServer.stop();
        httpServer.stop(0);
        Main.getLogger().info("Proxy servers stopped!");
    }
}
