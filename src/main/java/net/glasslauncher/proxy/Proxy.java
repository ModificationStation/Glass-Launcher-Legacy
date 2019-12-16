package net.glasslauncher.proxy;

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
    private HttpProxyServer server;

    public Proxy(boolean[] args) {
        try {
            // 1: args[sound, skin, cape]
            new File(Config.getGlassPath() + "proxyconf").mkdirs();

            ProxyHttpServer main = new ProxyHttpServer();
            main.start();

            this.serverBoot =
                    DefaultHttpProxyServer.bootstrap()
                            .withPort(Config.getProxyport())
                            .withManInTheMiddle(new CertificateSniffingMitmManager(new Authority(
                                    new File(Config.getGlassPath() + "proxyconf"),
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
            Main.getLogger().info("Log format for proxy is oldhost : oldpath : newhost : newurl");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        server = serverBoot.start();
    }

    public void exit() {
        server.stop();
    }
}
