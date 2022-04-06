package net.glasslauncher.legacy.mc;

import java.util.logging.*;

public class MinecraftFormatter extends Formatter {
    @Override
    public String format(LogRecord logRecord) {
        return logRecord.getMessage() + "\n";
    }
}
