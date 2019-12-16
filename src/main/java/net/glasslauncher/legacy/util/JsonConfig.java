package net.glasslauncher.legacy.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.lang.reflect.Type;

@Data
public abstract class JsonConfig {
    private final Gson gson = (new GsonBuilder()).setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private final String path;

    /**
     * @param path Path to the JSON file.
     */
    public JsonConfig(String path) {
        this.path = path;
    }

    public static JsonConfig loadConfig(String path, Type pClass) {
        try {
            return (new Gson()).fromJson(new FileReader(path), pClass);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Saves the JSON object stored in memory.
     */
    public void saveFile() {
        try (PrintStream out = new PrintStream(new FileOutputStream(path))) {
            out.print(gson.toJson(this));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
