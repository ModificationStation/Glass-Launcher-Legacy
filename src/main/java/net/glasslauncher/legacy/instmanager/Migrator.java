package net.glasslauncher.legacy.instmanager;

import net.glasslauncher.legacy.Main;
import net.glasslauncher.legacy.util.DirFilenameFilter;

import java.io.File;
import java.util.Objects;

import static org.apache.commons.io.FileUtils.moveDirectory;

public class Migrator {
    private File oldpath;
    private File newpath;

    /**
     * Sets up the migrator.
     * @param oldpath The path to take data from. Must be a PyMCL install location.
     * @param newpath The path that data is imported to. Must be a Glass Launcher install location.
     */
    public Migrator(String oldpath, String newpath) {
        this.oldpath = new File(oldpath);
        this.newpath = new File(newpath);
    }

    /**
     * @// TODO: 20/09/2019 Finish importing the entire file structure and porting JSONs to new formats.
     */
    public void migrate() {
        String[] instances = Objects.requireNonNull(new File(oldpath, "instances").list(new DirFilenameFilter()));
        if (instances.length < 1) {
            Main.getLogger().severe("No instances found! Aborting.");
            return;
        }

        for (String instance : instances) {
            File newpath = new File(this.newpath + "/instances/" + instance);
            File oldpath = new File(this.oldpath + "/instances/" + instance);
            if (!newpath.exists()) {
                try {
                    moveDirectory(oldpath, newpath);
                } catch (Exception e) {
                    Main.getLogger().severe("Failed to migrate \"" + instance + "\"");
                    e.printStackTrace();
                }
            } else {
                Main.getLogger().warning("Skipping \"" + instance + "\": File or directory already exists");
            }
        }
    }
}
