package net.glass.glassl.mc;

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.glass.glassl.Main;
import org.littleshoot.proxy.HttpFiltersAdapter;

import java.net.MalformedURLException;
import java.net.URL;

public class ProxyFilter extends HttpFiltersAdapter {
    private static String newHost = "resourceproxy.pymcl.net";
    private boolean doSoundFix;
    private boolean doSkinFix;
    private boolean doCapeFix;

    ProxyFilter(HttpRequest originalRequest, boolean[] args) {
        super(originalRequest);
        this.doSoundFix = args[0];
        this.doSkinFix = args[1];
        this.doCapeFix = args[2];
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        try {
            System.out.println(((HttpRequest) httpObject).getUri());
        }
        catch (Exception ignored) {}

        if(httpObject instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) httpObject;
            String host = httpRequest.headers().get("Host");
            String path;
            if (httpRequest.getUri().startsWith("http://") || httpRequest.getUri().startsWith("https://")) {
                if (httpRequest.getUri().contains("pymcl.net")) {
                    return null;
                }

                try {
                    path = new URL(httpRequest.getUri()).getPath();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                }

                String end = null;

                if (doSoundFix && (path.contains("MinecraftResources") || path.contains("/resources/"))) {
                    end = path;
                }

                if (doSkinFix && path.contains("MinecraftSkins")) {
                    end = "/skinapi.php?user=" + path.split("/")[2];
                }

                if (doCapeFix && path.contains("MinecraftCloaks")) {
                    end = "/capeapi.php?user=" + path.split("/")[2];
                }

                if (host.contains("minecraft.net")) {
                    if (doSkinFix && path.contains("skin")) {
                        end = "/skinapi.php?user=" + path.split("/")[2];
                    }

                    if (doCapeFix && path.contains("cloak")) {
                        end = "/capeapi.php?user=" + path.split("=")[1];
                    }
                }

                if (end == null) {
                    return null;
                }

                httpRequest.setUri("http://" + newHost + end);
                httpRequest.headers().set("Host", newHost);
                Main.logger.info(host + " : " + path + " : " + httpRequest.headers().get("Host") + " : " + httpRequest.getUri());
            }
        }
        return null;
    }
}
