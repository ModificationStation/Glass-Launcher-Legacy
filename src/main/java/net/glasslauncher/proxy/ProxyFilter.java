package net.glasslauncher.proxy;

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;
import org.littleshoot.proxy.HttpFiltersAdapter;

import java.util.List;

public class ProxyFilter extends HttpFiltersAdapter {
    private static List<String> ignoredHosts = Config.PROXY_IGNORED_HOSTS;
    private static String newHost = "localhost:" + Config.PROXY_WEB_PORT;
    private boolean doSoundFix;
    private boolean doSkinFix;
    private boolean doCapeFix;
    private boolean doLoginFix;

    ProxyFilter(HttpRequest originalRequest) {
        super(originalRequest);
    }

    public void setArgs(boolean[] args) {
        this.doSoundFix = args[0];
        this.doSkinFix = args[1];
        this.doCapeFix = args[2];
        this.doLoginFix = args[3];
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {

        if (httpObject instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) httpObject;
            String host = httpRequest.headers().get("Host");
            String path;

            if (httpRequest.getUri().startsWith("http://") || httpRequest.getUri().startsWith("https://")) {
                String uri = httpRequest.getUri();
                for (String ignoredHost : ignoredHosts) {
                    if (uri.contains(ignoredHost)) {
                        return null;
                    }
                }

                try {
                    path = httpRequest.getUri().replaceFirst("htt[ps]*://" + host, "");
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

                String doRedirect = null;

                if (host.contains("amazonaws.com") || host.contains("minecraft.net")) {
                    if (doSoundFix && (path.contains("MinecraftResources") || path.contains("resources"))) {
                        if (path.equals("/resources/")) {
                            path = "/MinecraftResources/indexalpha.php";
                        }
                        path = path.replaceFirst("/resources/", "/MinecraftResources/");
                        httpRequest.setUri("http://mcresources.modification-station.net" + path);
                        httpRequest.headers().set("Host", "mcresources.modification-station.net");
                        return null;
                    }

                    if (doSkinFix && path.contains("MinecraftSkins")) {
                        doRedirect = "/skins/" + path.split("/")[2];
                    }

                    if (doCapeFix && path.contains("MinecraftCloaks")) {
                        doRedirect = "/capes/" + path.split("/")[2];
                    }
                }

                if (host.contains("minecraft.net")) {
                    if (doSkinFix && path.contains("/skin/")) {
                        doRedirect = "/skins/" + path.split("/")[2];
                    }

                    if (doCapeFix && path.contains("/cloak/")) {
                        doRedirect = "/capes/" + path.split("/")[2];
                    }

                    if (doLoginFix && path.contains("game/joinserver")) {
                    	doRedirect = path.replaceFirst("game/joinserver.jsp?", "join/");
                    }

                    if (doLoginFix && path.contains("game/checkserver")) {
                    	doRedirect = path.replaceFirst("game/checkserver.jsp?", "join/");
                    }
                }

                if (doRedirect == null) {
                    return null;
                }

                httpRequest.setUri("http://" + newHost + doRedirect);
                httpRequest.headers().set("Host", newHost);
            }
        }
        return null;
    }
}
