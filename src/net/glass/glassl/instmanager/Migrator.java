package net.glass.glassl.instmanager;

import net.glass.glassl.Main;
import net.glass.glassl.util.DirFilenameFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Migrator {
    private File oldpath;
    private File newpath;

    public Migrator(String oldpath, String newpath) {
        this.oldpath = new File(oldpath);
        this.newpath = new File(newpath);
        Main.logger.info(oldpath);
    }

    public void migrate() {
        String[] instances = new File(oldpath, "instances").list(new DirFilenameFilter());
        if (instances.length < 1) {
            Main.logger.severe("No instances found! Aborting.");
            return;
        }

        for (String instance : instances) {
            File newpath = new File(this.newpath + "/instances/" + instance);
            File oldpath = new File(this.oldpath + "/instances/" + instance);
            if (!newpath.exists()) {
                try {
                    org.apache.commons.io.FileUtils.moveDirectory(oldpath, newpath);
                } catch (Exception e) {
                    Main.logger.severe("Failed to migrate \"" + instance + "\"");
                    e.printStackTrace();
                }
            }
            else {
                Main.logger.warning("Skipping \"" + instance + "\": File or directory already exists");
            }
        }
    }
}
