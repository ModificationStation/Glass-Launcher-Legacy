/*
Credit to:
https://stackoverflow.com/a/32351355
 */
package net.glass.glassl.mc;

import java.io.*;

public class StreamGobbler extends Thread {
    private InputStream in;
    private PrintStream out;

    StreamGobbler(InputStream in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = input.readLine()) != null)
                out.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}