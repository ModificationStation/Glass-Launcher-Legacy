/*
Credit to:
https://stackoverflow.com/a/32351355
 */
package net.glasslauncher.legacy.mc;

import java.io.*;
import java.util.logging.*;

public class MinecraftLogInterceptor extends Thread {
    private InputStream in;
    private Logger out;
    private boolean isErr;

    MinecraftLogInterceptor(InputStream in, Logger out, boolean isErr) {
        this.in = in;
        this.out = out;
        this.isErr = isErr;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = input.readLine()) != null)
                if (isErr)
                    out.severe(line);
                else
                    out.info(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}