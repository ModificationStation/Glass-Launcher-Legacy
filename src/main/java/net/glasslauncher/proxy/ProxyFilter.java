package net.glasslauncher.proxy;

import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.glasslauncher.legacy.Config;
import org.littleshoot.proxy.HttpFiltersAdapter;

public class ProxyFilter extends HttpFiltersAdapter {
    private static String[] ignoredHosts = Config.getProxyIgnoredHosts();
    private static String newHost = "localhost:" + Config.getProxywebport();
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

                if (host.contains("amazonaws.com")) {
                	System.out.println("amazonaws.com");
                    if (doSoundFix && (path.contains("MinecraftResources") || path.contains("/resources/"))) {
                        httpRequest.setUri("http://resourceproxy.pymcl.net" + path);
                        httpRequest.headers().set("Host", "resourceproxy.pymcl.net");
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
                	System.out.println("minecraft.net");
                    if (doSkinFix && path.contains("skin")) {
                        doRedirect = "/skins/" + path.split("/")[2];
                    }

                    if (doCapeFix && path.contains("cloak")) {
                        doRedirect = "/capes/" + path.split("/")[2];
                    }

                    if (doLoginFix && path.contains("game/joinserver")) {
                    	System.out.println("joinserver");
                    	doRedirect = path.replaceFirst("game/joinserver.jsp?", "join/");
                    }

                    if (doLoginFix && path.contains("game/checkserver")) {
                    	System.out.println("checkserver");
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
