package net.glass.glassl.mc;

import net.glass.glassl.Main;

import java.util.Timer;
import java.util.TimerTask;

public class Monitor {
    private TimerTask task;

    private Timer timer = new Timer();

    Monitor(Process mc, Proxy proxy) {

        task = new TimerTask() {
            @Override
            public void run() {
                if (!mc.isAlive()) {
                    if (proxy != null) {
                        proxy.exit();
                    }
                    Main.logger.info("Minecraft exited with exit code " + mc.exitValue());
                    timer.cancel();
                }
            }
        };
    }

    public void start() {
        timer.schedule(task, 0, 1000);
    }
}
