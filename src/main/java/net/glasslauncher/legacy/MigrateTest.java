package net.glasslauncher.legacy;

import net.glasslauncher.legacy.instmanager.Migrator;

public class MigrateTest {
    public static void main(String[] args) {
        Migrator migrator = new Migrator(Config.pymclpath, Config.glasspath);
        migrator.migrate();
    }
}
