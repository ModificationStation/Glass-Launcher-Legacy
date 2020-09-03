package net.glasslauncher.legacy;

import lombok.Getter;
import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.FileUtils;

import javax.swing.UIManager;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Main {
    @Getter private static Logger logger = CommonConfig.makeLogger("GlassLauncher", "glass-launcher");
    private static ArrayList<String> libs = new ArrayList<>();
    public static MainWindow mainwin;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ConsoleWindow console = new ConsoleWindow();

        try {
            Config.loadConfigFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String arg : args) {
            if (arg.equals("-proxy")) {
                ProxyStandalone.main(args);
                return;
            }
        }
        /*try {
            Main.logger.info(RepoReader.getMods()[0].getName());
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        mainwin = new MainWindow(console);
    }

    /**
     * Checks if the main launcher window is active.
     *
     * @return True if active, False otherwise.
     */
    public static boolean isLauncherActive() {
        return mainwin != null;
    }
}
