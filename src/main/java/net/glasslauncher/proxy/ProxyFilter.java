package net.glasslauncher.proxy;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;
import net.glasslauncher.legacy.Main;
import org.littleshoot.proxy.HttpFiltersAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class ProxyFilter extends HttpFiltersAdapter {
    private static String newHost = "localhost:25561";
    private boolean doSoundFix;
    private boolean doSkinFix;
    private boolean doCapeFix;

    ProxyFilter(HttpRequest originalRequest) {
        super(originalRequest);
    }

    public void setArgs(boolean[] args) {
        this.doSoundFix = args[0];
        this.doSkinFix = args[1];
        this.doCapeFix = args[2];
        Main.logger.info(Arrays.toString(args));
    }

    public HttpObject responsePre(HttpObject httpObject) {
        System.out.println("Response is: " + httpObject);

        if (httpObject instanceof HttpContent) {
            System.out.println(((HttpContent) httpObject)
                    .content().toString(CharsetUtil.UTF_8));
        }
        return httpObject;
    };


    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {

        if (httpObject instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) httpObject;
            String host = httpRequest.headers().get("Host");
            String path;

            Main.logger.info(httpRequest.getUri());
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

                String doRedirect = null;

                if (doSoundFix && (path.contains("MinecraftResources") || path.contains("/resources/"))) {
                    doRedirect = path;
                }

                if (doSkinFix && path.contains("MinecraftSkins")) {
                    doRedirect = "/skins/" + path.split("/")[2];
                }

                if (doCapeFix && path.contains("MinecraftCloaks")) {
                    doRedirect = "/capes/" + path.split("/")[2];
                }

                if (host.contains("minecraft.net")) {
                    if (doSkinFix && path.contains("skin")) {
                        doRedirect = "/skins/" + path.split("/")[2];
                    }

                    if (doCapeFix && path.contains("cloak")) {
                        doRedirect = "/capes/" + path.split("/")[2];
                    }
                }

                if (doRedirect == null) {
                    Main.logger.info("Nulled!");
                    return null;
                }

                httpRequest.setUri("http://" + newHost + doRedirect);
                httpRequest.headers().set("Host", newHost);
                Main.logger.info(host + " : " + path + " : " + httpRequest.headers().get("Host") + " : " + httpRequest.getUri());
            }
        }
        return null;
    }
}
