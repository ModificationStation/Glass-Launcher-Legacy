/*
Credit to:
https://stackoverflow.com/a/32351355
 */
package net.glasslauncher.legacy.mc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

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