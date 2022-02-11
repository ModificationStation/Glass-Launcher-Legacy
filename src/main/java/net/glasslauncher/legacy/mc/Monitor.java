package net.glasslauncher.legacy.mc;

import net.glasslauncher.legacy.Main;

public class Monitor {
    private Thread task;

    Monitor(Process mc, Runnable onClose) {

        task = new Thread(() -> {
            while (true) {
                if (!mc.isAlive()) {
                    onClose.run();
                    Main.LOGGER.info("Minecraft exited with exit code " + mc.exitValue());
                    break;
                }
            }
        });
    }

    public void start() {
        task.start();
    }
}
