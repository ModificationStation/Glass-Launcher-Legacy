/*
This uses a buffer system because appending to a TextPane takes about 20ms.
 */

package net.glass.glassl;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

public class ConsoleStream extends OutputStream {
    private TextArea textArea;
    private PrintStream jcons;
    private Timer timer = new Timer();

    private String buffer = "";

    public ConsoleStream(TextArea textArea, PrintStream jcons) {
        this.textArea = textArea;
        this.jcons = jcons;
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (!buffer.isEmpty()) {
                            textArea.append(buffer);
                            textArea.update(textArea.getGraphics());
                            buffer = "";
                        }
                    }
                },
                0,
                100);
    }

    @Override
    public void write(int b) throws IOException {
        buffer += (char) b;
    }
}