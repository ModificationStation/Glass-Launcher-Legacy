package net.glasslauncher.legacy;

import lombok.Getter;
import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.FileUtils;
import net.glasslauncher.repo.api.mod.RepoReader;

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

        getDeps();
        Config.setEasyMineLauncherFile(libs.get(0));

        // TODO: fix bcp* libs breaking for no reason.
        /*for (Object lib : libs.toArray()) {
            try {
                Classpath.addFile(Config.GLASS_PATH + "lib/" + lib);
            } catch (Exception e) {
                logger.info("Failed to load \"" + lib + "\".");
                e.printStackTrace();
            }
        }*/

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
        try {
            Main.logger.info(RepoReader.getMods()[0].getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mainwin = new MainWindow(console);
    }

    /**
     * Downloads all dependencies listed in Config.Deps.cactusDeps.
     */
    private static void getDeps() {
        getLogger().info("Checking dependencies...");

        for (String dep : Config.getGLASS_DEPS().keySet()) {
            try {
                FileUtils.downloadFile(dep, CommonConfig.GLASS_PATH + "/lib/", Config.getGLASS_DEPS().get(dep));
                libs.add(dep.substring(dep.lastIndexOf('/') + 1));
            } catch (Exception e) {
                getLogger().info("Failed to download dependency. Invalid formatting?");
                e.printStackTrace();
            }
        }
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
