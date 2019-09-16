/*
This is a heavily modified version of BetaCraft's wrapper.
https://github.com/Moresteck/BetaCraft-Launcher-Java/blob/master/betacraft-wrapper/org/betacraft/Wrapper.java
 */

package archive;

import net.glass.glassl.Config;
import net.glass.glassl.util.ComponentArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.logging.*;

public class GWrapper extends Applet implements AppletStub {
    private final static Map<String, String> params = new HashMap<>(); // Custom parameters
    private static List<String> nonOnlineClassic = new ArrayList<>(); // A list of classic MC versions that don't support MP
    private static int context = 0; // Return value for isActive
    static Logger logger = Logger.getLogger("wrapper"); // Logger. Defunct when ran by the main program.
    private boolean active = false;

    private static String instance;
    private static Applet applet = null;
    private static URLClassLoader classLoader;
    private static String ver_prefix;
    private static ComponentArrayList widgetlist;

    public static void main(String[] args, ComponentArrayList widgetlist) {
        // 0: username, 1: session, 2: version, 3: proxy, 4: instance
        makeLogger();
        logger.info("Starting GlassWrapper v0.1");

        try {
            if (args.length < 4) {
                JOptionPane.showMessageDialog(null, "Error code 1: Could not initialize wrapper (arguments too short)", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Our wrapper arguments are differenciated by colons
            logger.info("Wrapper arguments: " + Arrays.toString(args));

            // Add parameters for the client
            params.put("username", args[0]);
            params.put("sessionid", args[1]);
            params.put("haspaid", "true");

            String version = args[2];

            // Fix crashing when teleporting with Java 8+
            System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

            // Turn on proxy if wanted
            if (args[3].equals("true")) {
                if (version.startsWith("c0.")) {
                    // We gonna use Retrocraft for Classic for now :(
                    System.setProperty("http.proxyHost", "classic.retrocraft.net");
                    System.setProperty("http.proxyPort", "80");
                } else {
                    //System.setProperty("http.proxyHost", "localhost");
                    //System.setProperty("http.proxyPort", Config.proxyport);
                    logger.info("Proxy settings not applied, proxy doesn't work.");
                }
            }

            instance = args[4];

            // Make a prefix for the version
            if (version.startsWith("c0.")) {
                ver_prefix = "c";
            }
            else {
                ver_prefix = "";
            }

            // Allow joining servers for Classic MP versions
            if (ver_prefix.equals("c") && !nonOnlineClassic.contains(version)) {
                String server = JOptionPane.showInputDialog(null, "Input server address:", "");
                String port = "25565";
                String ip = server;
                if (ip.contains(":")) {
                    String[] params1 = server.split(":");
                    ip = params1[0];
                    port = params1[1];
                }
                if (!server.equals("")) {
                    System.out.println("Accepted server parameters: " + server);
                    params.put("server", ip);
                    params.put("port", port);
                }
            }

            // Start the game
            new GWrapper().play();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void play() {
        final String mcpath = Config.glasspath + "instances/" + instance;

        System.setProperty("net.java.games.input.librarypath", mcpath + "/.minecraft/bin/natives");
        System.setProperty("org.lwjgl.librarypath", mcpath + "/.minecraft/bin/natives");
        System.setProperty("user.home", mcpath);
        System.setProperty("home", mcpath);
        Map<String, String> newenv = new HashMap<>();
        newenv.put("appdata", mcpath);
        try {
            setEnv(newenv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            final URL[] jars = new URL[4];
            jars[0] = new File(mcpath + "/.minecraft/bin/minecraft.jar").toURI().toURL();
            jars[1] = new File(mcpath + "/.minecraft/bin/lwjgl.jar").toURI().toURL();
            jars[2] = new File(mcpath + "/.minecraft/bin/lwjgl_util.jar").toURI().toURL();
            jars[3] = new File(mcpath + "/.minecraft/bin/jinput.jar").toURI().toURL();

            classLoader = new URLClassLoader(jars);
            Class client = classLoader.loadClass("net.minecraft.client.MinecraftApplet");
            Applet applet = (Applet) client.newInstance();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                applet.stop();
                applet.destroy();
            }));
            // Frame for game
            final Frame gameFrame = new Frame();
            gameFrame.setTitle("Minecraft " + ver_prefix);
            BufferedImage img = ImageIO.read(GWrapper.class.getResourceAsStream("assets/favicon.ico"));
            gameFrame.setIconImage(img);
            gameFrame.setBackground(Color.BLACK);

            // This is needed for the window size
            final JPanel panel = new JPanel();
            gameFrame.setLayout(new BorderLayout());
            panel.setPreferredSize(new Dimension(854, 480));
            gameFrame.add(panel, "Center");
            gameFrame.pack();
            gameFrame.setLocationRelativeTo(null);
            gameFrame.setVisible(true);

            applet.setStub(this);

            gameFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    new Thread(() -> {
                        stop();
                        destroy();
                        gameFrame.setVisible(false);
                        gameFrame.dispose();
                        try {
                            classLoader.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }).start();
                }
            });
            this.setLayout(new BorderLayout());
            this.add(applet, "Center");
            this.validate();
            gameFrame.removeAll();
            gameFrame.setLayout(new BorderLayout());
            gameFrame.add(this, "Center");
            gameFrame.validate();


            applet.init();
            active = true;
            applet.start();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void appletResize(int arg0, int arg1) {
    }

    @Override
    public void update(final Graphics g) {
        this.paint(g);
    }

    @Override
    public boolean isActive() {
        if (context == 0) {
            context = -1;
            try {
                if (this.getAppletContext() != null) {
                    context = 1;
                }
            } catch (Exception ignored) {}
        }
        if (context == -1) {
            return active;
        }
        return super.isActive();
    }

    @Override
    public void stop() {
        // Shutdown the RPC correctly
        if (applet != null) {
            active = false;
            applet.stop();
        }
    }

    @Override
    public void destroy() {
        if (applet != null) {
            applet.destroy();
        }
        widgetlist.setEnabledAll(true);
    }

    @Override
    public void start() {
        if (applet != null) {
            applet.start();
        }
    }

    @Override
    public void init() {
        if (applet != null) {
            applet.init();
        }
    }

    @Override
    public URL getDocumentBase() {
        try {
            return new URL("http://www.minecraft.net/game/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public URL getCodeBase() {
        try {
            return new URL("http://www.minecraft.net/game/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getParameter(final String paramName) {
        if (params.containsKey(paramName)) {
            return params.get(paramName);
        }
        return null;
    }

    /*
    This is a very hacky and nasty piece of code.
    But it does exactly what I need it to.
    Credit: https://stackoverflow.com/a/7201825
    Used to force %appdata% to be a custom dir, meaning things like Audiomod will work on windows.
    */
    private static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }

    private static void makeLogger() {
        try {
            System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [GlassW][%4$s] %5$s %n");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();
            String time = dtf.format(now);
            File logdir = new File(Config.glasspath + "/glass-logs/wrapper");
            logdir.mkdirs();
            Handler file_handler = new FileHandler(Config.glasspath + "/glass-logs/wrapper/" + time + ".log");
            SimpleFormatter format = new SimpleFormatter();
            logger.addHandler(file_handler);
            file_handler.setFormatter(format);
            logger.setLevel(Level.ALL);
            file_handler.setLevel(Level.ALL);
            logger.info("Logging to " + logdir.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        nonOnlineClassic.add("c0.0.1a");
        nonOnlineClassic.add("c0.0.2a");
        nonOnlineClassic.add("c0.0.3a");
        nonOnlineClassic.add("c0.0.4a");
        nonOnlineClassic.add("c0.0.5a");
        nonOnlineClassic.add("c0.0.6a");
        nonOnlineClassic.add("c0.0.7a");
        nonOnlineClassic.add("c0.0.8a");
        nonOnlineClassic.add("c0.0.9a");
        nonOnlineClassic.add("c0.0.10a");
        nonOnlineClassic.add("c0.0.11a");
        nonOnlineClassic.add("c0.0.12a-dev");
        nonOnlineClassic.add("c0.0.12a");
        nonOnlineClassic.add("c0.0.12a_01");
        nonOnlineClassic.add("c0.0.12a_02");
        nonOnlineClassic.add("c0.0.12a_03");
        nonOnlineClassic.add("c0.0.13a-dev");
        nonOnlineClassic.add("c0.0.13a");
        nonOnlineClassic.add("c0.0.13a_01");
        nonOnlineClassic.add("c0.0.13a_02");
        nonOnlineClassic.add("c0.0.13a_03");
        nonOnlineClassic.add("c0.0.14a");
        nonOnlineClassic.add("c0.0.14a_01");
        nonOnlineClassic.add("c0.0.14a_02");
        nonOnlineClassic.add("c0.0.14a_03");
        nonOnlineClassic.add("c0.0.14a_04");
        nonOnlineClassic.add("c0.0.14a_06");
        nonOnlineClassic.add("c0.0.14a_07");
        nonOnlineClassic.add("c0.0.14a_08");
    }
}
