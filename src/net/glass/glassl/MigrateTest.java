package net.glass.glassl;

import net.glass.glassl.instmanager.Migrator;

public class MigrateTest {
    public static void main(String[] args) {
        Migrator migrator = new Migrator(Config.pymclpath, Config.glasspath);
        migrator.migrate();
    }
}
