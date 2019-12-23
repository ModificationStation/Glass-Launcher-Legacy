package net.glasslauncher.legacy;

import net.glasslauncher.legacy.instmanager.Migrator;

public class MigrateTest {
    public static void main(String[] args) {
        Migrator migrator = new Migrator(Config.PYMCL_PATH, Config.GLASS_PATH);
        migrator.migrate();
    }
}
