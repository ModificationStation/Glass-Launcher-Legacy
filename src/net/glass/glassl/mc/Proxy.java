package net.glass.glassl.mc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import net.glass.glassl.Config;
import net.glass.glassl.Main;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.mitm.Authority;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.littleshoot.proxy.mitm.RootCertificateException;

import java.io.File;

public class Proxy extends Thread {
    private HttpProxyServerBootstrap serverBoot;
    private HttpProxyServer server;

    public Proxy(boolean[] args) {
        try {
            // 1: args[sound, skin, cape]
            new File(Config.glasspath + "proxyconf").mkdirs();

            this.serverBoot =
                    DefaultHttpProxyServer.bootstrap()
                            .withAllowRequestToOriginServer(true)
                            .withPort(Config.proxyport)
                            .withManInTheMiddle(new CertificateSniffingMitmManager(new Authority(
                                    new File(Config.glasspath + "proxyconf"),
                                    "glass-proxy-mitm",
                                    "thisisranlocallysothisdoesntmatter".toCharArray(),
                                    "Glass",
                                    "Glass",
                                    "Glass-proxy, a simple proxy to fix legacy MC.",
                                    "Glass",
                                    "Glass-proxy, used by Glass-launcher to fix sounds, skins and capes in legacy Minecraft."
                            )))
                            .withFiltersSource(new HttpFiltersSourceAdapter() {
                                public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                                    ProxyFilter proxyFilter = new ProxyFilter(originalRequest);
                                    proxyFilter.setArgs(args);
                                    return proxyFilter;
                                }
                            });
            Main.logger.info("Log format for proxy is oldhost : oldpath : newhost : newurl");
        }
        catch (RootCertificateException e) {
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
