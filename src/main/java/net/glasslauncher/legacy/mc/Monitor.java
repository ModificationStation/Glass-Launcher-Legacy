package net.glasslauncher.legacy.mc;

import net.glasslauncher.legacy.Main;

import java.io.*;
import java.util.function.*;

public class Monitor {
    private final Thread task;

    Monitor(ProcessBuilder processBuilder, Consumer<Process> onStart, Runnable onClose) throws IOException {
        Process mc = processBuilder.start();
        onStart.accept(mc);
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
