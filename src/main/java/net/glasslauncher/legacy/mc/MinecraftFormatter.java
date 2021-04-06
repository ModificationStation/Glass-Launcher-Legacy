package net.glasslauncher.legacy.mc;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MinecraftFormatter extends Formatter {
    @Override
    public String format(LogRecord logRecord) {
        return logRecord.getMessage() + "\n";
    }
}
