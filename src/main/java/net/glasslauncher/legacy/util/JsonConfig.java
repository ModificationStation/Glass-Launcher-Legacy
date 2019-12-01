package net.glasslauncher.legacy.util;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import net.glasslauncher.legacy.Config;
import net.glasslauncher.legacy.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;

public class JsonConfig {
    private JsonObject jsonObject;
    private String path;

    /**
     * Reads JSON file from disk.
     * Creates an empty JSON object if file can't be read.
     *
     * @param path Path to the JSON file.
     */
    public JsonConfig(String path) {
        this.path = path;
        try {
            boolean isJar;
            if (path.startsWith("jar:")) {
                isJar = true;
            } else {
                isJar = false;
            }
            if ((new File(path)).exists()) {
                this.jsonObject = (JsonObject) JsonReader.jsonToJava(FileUtils.readFile(path, isJar));
            } else {
                this.jsonObject = new JsonObject();
            }
        }
        catch (Exception e) {
            Main.logger.info("Failed to read JSON file:");
            e.printStackTrace();
            this.jsonObject = new JsonObject();
        }
    }

    /**
     * Reads JSON file from disk.
     * Creates JSON object with supplied JSON encoded string if file can't be read.
     *
     * @param path Path to the JSON file.
     */
    public JsonConfig(String path, String defaultJSON) {
        this.path = path;
        try {
            this.jsonObject = (JsonObject) JsonReader.jsonToJava(FileUtils.readFile(path));
        }
        catch (Exception e) {
            Main.logger.info("Failed to read JSON file:");
            e.printStackTrace();
            this.jsonObject = (JsonObject) JsonReader.jsonToJava(defaultJSON);
        }
    }

    /**
     * Gets object from given key object.
     *
     * @param key Key object to get the object with.
     * @return Returns the object that was found.
     * @throws NullPointerException Thrown if found object is null.
     */
    public Object get(Object key) throws NullPointerException {
        Object obj = jsonObject.get(key);
        if (obj == null) {
            throw new NullPointerException("Value does not exist.");
        }
        return obj;
    }


    /**
     * Gets object from given key object.
     * Returns defaultObj if object found by key is null.
     *
     * @param key Target JSON key to get the object with.
     * @param defaultObj Object to return if object found be key is null.
     * @return Returns the object that was found.
     */
    public Object get(Object key, Object defaultObj) {
        Object obj = defaultObj;
        try {
            obj = jsonObject.get(key);
        }
        catch (Exception e) {
            set(key, defaultObj);
        }
        return obj;
    }

    /**
     * Sets given key object to value object.
     *
     * @param key Target JSON key.
     * @param value Value for JSON key.
     */
    public void set(Object key, Object value) {
        jsonObject.put(key, value);
    }

    /**
     * Gets keyset of the JSON object.
     *
     * @return The keyset of the JSON object.
     */
    public Set keySet() {
        return jsonObject.keySet();
    }

    /**
     * Saves the JSON object stored in memory.
     *
     * @return true on success, false on an error.
     */
    public boolean saveFile() {
        try (PrintStream out = new PrintStream(new FileOutputStream(path))) {
            out.print(JsonWriter.objectToJson(jsonObject, Config.prettyprint));
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
