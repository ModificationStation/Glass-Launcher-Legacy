package net.glasslauncher.legacy;

import net.glasslauncher.legacy.instmanager.Migrator;

public class MigrateTest {
    public static void main(String[] args) {
        Migrator migrator = new Migrator(Config.getPYMCL_PATH(), Config.getGLASS_PATH());
        migrator.migrate();
    }
}
