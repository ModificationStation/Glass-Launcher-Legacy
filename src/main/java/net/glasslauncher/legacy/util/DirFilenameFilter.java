package net.glasslauncher.legacy.util;

import java.io.File;
import java.io.FilenameFilter;

public class DirFilenameFilter implements FilenameFilter {

    @Override
    public boolean accept(File file, String s) {
        return new File(file, s).isDirectory();
    }
}
