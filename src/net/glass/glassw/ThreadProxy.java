/*
Doesn't work on minecraft, but works on tests with firefox. FML.
*/

package net.glass.glassw;

import net.glass.glassl.Config;

import java.io.*;
import java.net.*;

class ThreadProxy extends Thread {
    private Socket sClient;
    private ServerSocket sServer = new ServerSocket(Integer.parseInt(Config.proxyport));;

    private final boolean doSoundFix;
    private final boolean doSkinFix;
    private final boolean doCapeFix;
    ThreadProxy(boolean[] doproxy) throws IOException {
        this.doSoundFix = doproxy[0];
        this.doSkinFix = doproxy[1];
        this.doCapeFix = doproxy[2];
        this.start();
    }
    @Override
    public void run() {
        while (true) {
            try {
                sClient = sServer.accept();
                final byte[] request = new byte[1024];
                byte[] reply = new byte[8126];
                final InputStream inFromClient = sClient.getInputStream();
                final OutputStream outToClient = sClient.getOutputStream();
                final BufferedReader sin = new BufferedReader(new InputStreamReader(sClient.getInputStream()));
                // connects a socket to the server
                String firstline = sin.readLine();
                if (firstline == null) {
                    throw new NullPointerException("URL header empty!");
                }
                String clienturl = replaceLast(firstline.replaceFirst("GET ", ""), " HTTP/1.1", "");
                System.out.println(clienturl);
                String end = null;
                String path = new URL(clienturl).getPath();

                if (doSoundFix) {
                    if (path.contains("MinecraftResources") || path.contains("/resources/")) {
                        end = "/MinecraftResources";
                    }
                }

                if (doSkinFix) {
                    if (path.contains("MinecraftSkins")) {
                        end = "/skinapi.php?user=" + path.split("/")[2];
                    }
                }

                if (doCapeFix) {
                    if (path.contains("MinecraftCloaks")) {
                        end = "/capeapi.php?user=" + path.split("/")[2];
                    }
                }

                if (clienturl.contains("minecraft.net")) {
                    if (doSkinFix) {
                        if (path.contains("skin")) {
                            end = "/skinapi.php?user=" + path.split("/")[2];
                        }
                    }

                    if (doCapeFix) {
                        if (path.contains("cloak")) {
                            end = "/capeapi.php?user=" + path.split("=")[1];
                        }
                    }
                }

                URL url;
                if (end == null) {
                    url = new URL(clienturl);
                } else {
                    url = new URL("http://resourceproxy.pymcl.net" + end);
                }

                System.out.println(url.toString());
                URLConnection server;
                try {
                    server = url.openConnection();
                } catch (IOException e) {
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(
                            outToClient));
                    out.flush();
                    throw new RuntimeException(e);
                }
                server.setDoOutput(true);
                System.out.println("opened connection");
                // current thread to manage streams from client to server (UPLOAD)
                final OutputStream outToServer = server.getOutputStream();
                System.out.println("got output stream");
                try {
                    int bytes_read;
                    while ((bytes_read = inFromClient.read(request)) != -1) {
                        outToServer.write(request, 0, bytes_read);
                        outToServer.flush();
                    }
                } catch (IOException ignored) {}
                try {
                    outToServer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // new thread manages streams from server to client (DOWNLOAD)
                System.out.println("opening input stream");
                final InputStream inFromServer = server.getInputStream();
                System.out.println("got input steam");
                new Thread(() -> {
                    try {
                        int bytes_read;
                        System.out.println("start read");
                        while ((bytes_read = inFromServer.read(reply)) != -1) {
                            outToClient.write(reply, 0, bytes_read);
                            System.out.println("end buffer");
                        }
                        outToClient.flush();
                        System.out.println("end read");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            inFromServer.close();
                            outToServer.close();
                            inFromClient.close();
                            outToClient.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        sClient.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                    + replacement
                    + string.substring(pos + toReplace.length());
        } else {
            return string;
        }
    }
}
