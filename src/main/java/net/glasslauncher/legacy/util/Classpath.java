package net.glasslauncher.legacy.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Classpath {
    private static final Class[] PARAMETERS = new Class[]{URL.class};

    public static void addFile(String s) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        File f = new File(s);
        addFile(f);
    }

    public static void addFile(File f) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        addURL(f.toURI().toURL());
    }

    public static void addURL(URL u) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> sysclass = URLClassLoader.class;

        Method method = sysclass.getDeclaredMethod("addURL", PARAMETERS);
        method.setAccessible(true);
        method.invoke(sysloader, u);
    }
}
