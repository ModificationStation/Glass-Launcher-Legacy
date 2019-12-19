package net.glasslauncher.legacy.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import lombok.Data;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.lang.reflect.Type;

@Data
public abstract class JsonConfig {
    @Expose(serialize = false, deserialize = false) private String path;

    /**
     * @param path Path to the JSON file.
     */
    public JsonConfig(String path) {
        this.path = path;
    }

    public static JsonConfig loadConfig(String path, Type pClass) {
        try {
            FileReader fileReader = new FileReader(path);
            JsonConfig jsonObj = (new Gson()).fromJson(fileReader, pClass);
            fileReader.close();
            jsonObj.setPath(path);
            return jsonObj;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Saves the JSON object stored in memory.
     */
    public void saveFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            PrintStream out = new PrintStream(new FileOutputStream(path));
            out.print(gson.toJson(this));
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
