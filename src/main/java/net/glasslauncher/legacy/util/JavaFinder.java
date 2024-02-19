package net.glasslauncher.legacy.util;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import lombok.Getter;
import net.glasslauncher.common.LoggerFactory;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;

import java.io.*;
import java.util.*;

import static com.sun.jna.platform.win32.WinNT.KEY_WOW64_32KEY;
import static com.sun.jna.platform.win32.WinNT.KEY_WOW64_64KEY;

public class JavaFinder {

    public static ArrayList<JavaPath> findJavaPaths() {
        ArrayList<JavaPath> java_candidates = new ArrayList<>();

        // People might mess with this path, but it'll make linux user's lives a little easier in cases, so whatever.
        tryAddJavaPath(java_candidates, System.getProperty("java.home") + (Config.OS.equals("windows") ? "/bin/javaw.exe" : "/bin/javaw"));

        File[] jetbrainsJDKs = new File(System.getProperty("user.home") + "/.jdks").listFiles((file) -> !file.isFile());
        if(jetbrainsJDKs != null && jetbrainsJDKs.length > 0) {
            for (File path : jetbrainsJDKs) {
                tryAddJavaPath(java_candidates, path + (Config.OS.equals("windows") ? "/bin/javaw.exe" : "/bin/javaw"));
            }
        }

        if (Config.OS.equals("windows")) {
            // Oracle
            ArrayList<JavaPath> JRE64s = findJavaFromRegistryKey(KEY_WOW64_64KEY, "SOFTWARE\\JavaSoft\\Java Runtime Environment", "JavaHome");
            ArrayList<JavaPath> JDK64s = findJavaFromRegistryKey(KEY_WOW64_64KEY, "SOFTWARE\\JavaSoft\\Java Development Kit", "JavaHome");
            ArrayList<JavaPath> JRE32s = findJavaFromRegistryKey(KEY_WOW64_32KEY, "SOFTWARE\\JavaSoft\\Java Runtime Environment", "JavaHome");
            ArrayList<JavaPath> JDK32s = findJavaFromRegistryKey(KEY_WOW64_32KEY, "SOFTWARE\\JavaSoft\\Java Development Kit", "JavaHome");

            // Oracle for Java 9 and newer
            ArrayList<JavaPath> NEWJRE64s = findJavaFromRegistryKey(KEY_WOW64_64KEY, "SOFTWARE\\JavaSoft\\JRE", "JavaHome");
            ArrayList<JavaPath> NEWJDK64s = findJavaFromRegistryKey(KEY_WOW64_64KEY, "SOFTWARE\\JavaSoft\\JDK", "JavaHome");
            ArrayList<JavaPath> NEWJRE32s = findJavaFromRegistryKey(KEY_WOW64_32KEY, "SOFTWARE\\JavaSoft\\JRE", "JavaHome");
            ArrayList<JavaPath> NEWJDK32s = findJavaFromRegistryKey(KEY_WOW64_32KEY, "SOFTWARE\\JavaSoft\\JDK", "JavaHome");

            // AdoptOpenJDK
            ArrayList<JavaPath> ADOPTOPENJRE32s = findJavaFromRegistryKey(KEY_WOW64_32KEY, "SOFTWARE\\AdoptOpenJDK\\JRE", "Path", "\\hotspot\\MSI");
            ArrayList<JavaPath> ADOPTOPENJRE64s = findJavaFromRegistryKey(KEY_WOW64_64KEY, "SOFTWARE\\AdoptOpenJDK\\JRE", "Path", "\\hotspot\\MSI");
            ArrayList<JavaPath> ADOPTOPENJDK32s = findJavaFromRegistryKey(KEY_WOW64_32KEY, "SOFTWARE\\AdoptOpenJDK\\JDK", "Path", "\\hotspot\\MSI");
            ArrayList<JavaPath> ADOPTOPENJDK64s = findJavaFromRegistryKey(KEY_WOW64_64KEY, "SOFTWARE\\AdoptOpenJDK\\JDK", "Path", "\\hotspot\\MSI");

            // Eclipse Foundation
            ArrayList<JavaPath> FOUNDATIONJDK32s = findJavaFromRegistryKey(KEY_WOW64_32KEY, "SOFTWARE\\Eclipse Foundation\\JDK", "Path", "\\hotspot\\MSI");
            ArrayList<JavaPath> FOUNDATIONJDK64s = findJavaFromRegistryKey(KEY_WOW64_64KEY, "SOFTWARE\\Eclipse Foundation\\JDK", "Path", "\\hotspot\\MSI");

            // Eclipse Adoptium
            ArrayList<JavaPath> ADOPTIUMJRE32s = findJavaFromRegistryKey(KEY_WOW64_32KEY, "SOFTWARE\\Eclipse Adoptium\\JRE", "Path", "\\hotspot\\MSI");
            ArrayList<JavaPath> ADOPTIUMJRE64s = findJavaFromRegistryKey(KEY_WOW64_64KEY, "SOFTWARE\\Eclipse Adoptium\\JRE", "Path", "\\hotspot\\MSI");
            ArrayList<JavaPath> ADOPTIUMJDK32s = findJavaFromRegistryKey(KEY_WOW64_32KEY, "SOFTWARE\\Eclipse Adoptium\\JDK", "Path", "\\hotspot\\MSI");
            ArrayList<JavaPath> ADOPTIUMJDK64s = findJavaFromRegistryKey(KEY_WOW64_64KEY, "SOFTWARE\\Eclipse Adoptium\\JDK", "Path", "\\hotspot\\MSI");

            // Microsoft
            ArrayList<JavaPath> MICROSOFTJDK64s = findJavaFromRegistryKey(KEY_WOW64_64KEY, "SOFTWARE\\Microsoft\\JDK", "Path", "\\hotspot\\MSI");

            // Azul Zulu
            ArrayList<JavaPath> ZULU64s = findJavaFromRegistryKey(KEY_WOW64_64KEY, "SOFTWARE\\Azul Systems\\Zulu", "InstallationPath");
            ArrayList<JavaPath> ZULU32s = findJavaFromRegistryKey(KEY_WOW64_32KEY, "SOFTWARE\\Azul Systems\\Zulu", "InstallationPath");

            // BellSoft Liberica
            ArrayList<JavaPath> LIBERICA64s = findJavaFromRegistryKey(KEY_WOW64_64KEY, "SOFTWARE\\BellSoft\\Liberica", "InstallationPath");
            ArrayList<JavaPath> LIBERICA32s = findJavaFromRegistryKey(KEY_WOW64_32KEY, "SOFTWARE\\BellSoft\\Liberica", "InstallationPath");

            // List x64 before x86
            java_candidates.addAll(JRE64s);
            java_candidates.addAll(NEWJRE64s);
            java_candidates.addAll(ADOPTOPENJRE64s);
            java_candidates.addAll(ADOPTIUMJRE64s);
            tryAddJavaPath(java_candidates, "C:/Program Files/Java/jre8/bin/javaw.exe");
            tryAddJavaPath(java_candidates, "C:/Program Files/Java/jre7/bin/javaw.exe");
            tryAddJavaPath(java_candidates, "C:/Program Files/Java/jre6/bin/javaw.exe");
            java_candidates.addAll(JDK64s);
            java_candidates.addAll(NEWJDK64s);
            java_candidates.addAll(ADOPTOPENJDK64s);
            java_candidates.addAll(FOUNDATIONJDK64s);
            java_candidates.addAll(ADOPTIUMJDK64s);
            java_candidates.addAll(MICROSOFTJDK64s);
            java_candidates.addAll(ZULU64s);
            java_candidates.addAll(LIBERICA64s);

            java_candidates.addAll(JRE32s);
            java_candidates.addAll(NEWJRE32s);
            java_candidates.addAll(ADOPTOPENJRE32s);
            java_candidates.addAll(ADOPTIUMJRE32s);
            tryAddJavaPath(java_candidates, "C:/Program Files (x86)/Java/jre8/bin/javaw.exe");
            tryAddJavaPath(java_candidates, "C:/Program Files (x86)/Java/jre7/bin/javaw.exe");
            tryAddJavaPath(java_candidates, "C:/Program Files (x86)/Java/jre6/bin/javaw.exe");
            java_candidates.addAll(JDK32s);
            java_candidates.addAll(NEWJDK32s);
            java_candidates.addAll(ADOPTOPENJDK32s);
            java_candidates.addAll(FOUNDATIONJDK32s);
            java_candidates.addAll(ADOPTIUMJDK32s);
            java_candidates.addAll(ZULU32s);
            java_candidates.addAll(LIBERICA32s);

            // Clean up after ourselves.
            Advapi32Util.registryCloseKey(WinReg.HKEY_LOCAL_MACHINE);
        }

        java_candidates.addAll(addJavasFromEnv());

        return java_candidates;
    }


    static ArrayList<JavaPath> findJavaFromRegistryKey(int keyType, String keyName, String keyJavaDir) {
        return findJavaFromRegistryKey(keyType, keyName, keyJavaDir, "");
    }

    static ArrayList<JavaPath> findJavaFromRegistryKey(int keyType, String keyName, String keyJavaDir, String subkeySuffix) {
        ArrayList<JavaPath> javas = new ArrayList<>();

        try {
            // Get the number of subkeys
            String[] subKeys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, keyName);

            // Iterate until RegEnumKeyEx fails
            for (String key : subKeys) {
                String retVal = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, String.join("\\", new String[]{keyName, key + subkeySuffix}), keyJavaDir, keyType) + "\\bin\\javaw.exe";
                if (new File(retVal).isFile()) {
                    Optional<JavaPath> optionalJavaPath = getJavaPath(retVal);
                    optionalJavaPath.ifPresent(javas::add);
                }
            }
        } catch (Win32Exception e) {
            if (!e.getMessage().contains("cannot find")) {
                e.printStackTrace();
            }
        }

        return javas;
    }

    private static ArrayList<JavaPath> addJavasFromEnv() {
        ArrayList<JavaPath> javas = new ArrayList<>();
        for (String path : System.getenv("PATH").split(Config.OS.equals("windows") ? ";" : ":")) {
            String file = null;
            if(new File(path + "/javaw").exists()) {
                file = path + "/javaw";
            }
            else if(new File(path + "/javaw.exe").exists()) {
                file = path + "/javaw.exe";
            }
            if(file != null) {
                try {
                    Optional<JavaPath> optionalJavaPath = getJavaPath(file);
                    optionalJavaPath.ifPresent(javas::add);
                } catch (Exception e) {
                    Main.LOGGER.warning("Missing exec permissions, or not actually java at \"" + file + "\"");
                    e.printStackTrace();
                }
            }
        }
        return javas;
    }

    // For debugging the java finder.
    public static void main(String[] args) {
        Main.LOGGER = LoggerFactory.makeLogger("GlassLauncher", "glass-launcher");

        for (JavaPath key : findJavaPaths()) {
            Main.LOGGER.info(key.javaPath);
            Main.LOGGER.info(key.arch.value);
        }
    }

    private static boolean checkJavaCommand(String file) throws IOException {
        Process process = Runtime.getRuntime().exec(new String[]{file, "-version"});
        // Why the fuck do you print to stderr for this, Java?
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String s;
        boolean is64 = false;
        while ((s = stdError.readLine()) != null) {
            if (s.contains("64-Bit")) {
                is64 = true;
            }
        }
        return is64;
    }

    private static void tryAddJavaPath(ArrayList<JavaPath> javas, String path) {
        if(new File(path).exists()) {
            Optional<JavaPath> optionalJavaPath = getJavaPath(path);
            optionalJavaPath.ifPresent(javas::add);
        }
    }

    private static Optional<JavaPath> getJavaPath(String javaPath) {
        if(!(new File(javaPath)).exists()) {
            Main.LOGGER.warning("\"" + javaPath + "\" doesn't exist?");
            return Optional.empty();
        }

        Arch arch = Arch.unknown;

        try {
            arch = checkJavaCommand(javaPath) ? Arch.x64 : Arch.x32;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.of(new JavaPath(javaPath, arch));
    }

    public enum Arch {
        x64("64"),
        x32("32"),
        unknown("unknown");

        public final String value;
        Arch(String s) {
            value = s;
        }
    }

    @Getter
    public static class JavaPath {
        private final String javaPath;
        private final Arch arch;

        public JavaPath(String javaPath, Arch arch) {
            this.javaPath = javaPath;
            this.arch = arch;
        }
    }
}
