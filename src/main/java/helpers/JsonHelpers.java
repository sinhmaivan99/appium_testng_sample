package helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import constants.ConfigData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility for reading and writing JSON data files.
 * <p>
 * Provides methods for both JSON Object and JSON Array structures.
 * Uses Jackson for reads and Gson for writes (consistent with original).
 * </p>
 */
public final class JsonHelpers {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonHelpers() {
        // Utility class — prevent instantiation
    }

    // ═══════════════════════ READ HELPERS ═══════════════════════

    /**
     * Reads a nested value from the default JSON data file using dot-separated keys.
     *
     * @param keys path to the target node, e.g. "user", "name"
     * @return the text value at that path, or empty string if not found
     */
    public static String getValueJsonObject(String... keys) {
        return getValueJsonObjectFromFile(ConfigData.JSON_DATA_FILE_PATH, keys);
    }

    /**
     * Reads a nested value from a specific JSON file using dot-separated keys.
     */
    public static String getValueJsonObjectFromFile(String filePath, String... keys) {
        try {
            JsonNode node = MAPPER.readTree(new File(filePath));
            for (String key : keys) {
                node = node.path(key);
            }
            String value = node.asText();
            LogUtils.debug("JSON read [" + String.join(".", keys) + "] = " + value);
            return value;
        } catch (IOException e) {
            LogUtils.error("Failed to read JSON: " + filePath, e);
            return "";
        }
    }

    /**
     * Reads a value from a JSON array at the given index and path.
     *
     * @param itemIndex the index in the root JSON array
     * @param keys      path within the array item
     */
    public static String getValueJsonArray(int itemIndex, String... keys) {
        return getValueJsonArrayFromFile(ConfigData.JSON_DATA_FILE_PATH, itemIndex, keys);
    }

    /**
     * Reads a value from a JSON array in a specific file.
     */
    public static String getValueJsonArrayFromFile(String filePath, int itemIndex, String... keys) {
        try {
            JsonNode rootNode = MAPPER.readTree(new File(filePath));
            JsonNode item = rootNode.get(itemIndex);
            if (item == null || !item.isObject()) {
                throw new IllegalArgumentException("Invalid item index: " + itemIndex);
            }
            JsonNode current = item;
            for (String key : keys) {
                current = current.path(key);
            }
            String value = current.asText();
            LogUtils.debug("JSON array[" + itemIndex + "] [" + String.join(".", keys) + "] = " + value);
            return value;
        } catch (IOException e) {
            LogUtils.error("Failed to read JSON array: " + filePath, e);
            return "";
        }
    }

    // ═══════════════════════ UPDATE OBJECT ═══════════════════════

    /**
     * Updates a key in the root JSON object of the default data file.
     */
    public static void updateValueJsonObject(String key, String value) {
        updateValueJsonObject(ConfigData.JSON_DATA_FILE_PATH, key, value);
    }

    public static void updateValueJsonObject(String key, Number value) {
        updateValueJsonObject(ConfigData.JSON_DATA_FILE_PATH, key, value);
    }

    /**
     * Updates a key in the root JSON object of the given file.
     */
    public static void updateValueJsonObject(String filePath, String key, String value) {
        JsonObject obj = readJsonObject(filePath);
        logUpdateAction(obj, key);
        obj.addProperty(key, value);
        writeJsonObject(filePath, obj);
    }

    public static void updateValueJsonObject(String filePath, String key, Number value) {
        JsonObject obj = readJsonObject(filePath);
        obj.addProperty(key, value);
        writeJsonObject(filePath, obj);
    }

    /**
     * Updates a nested key ({@code parentKey.childKey}) in the default data file.
     */
    public static void updateNestedValueJsonObject(String parentKey, String childKey, String value) {
        updateNestedJsonObject(ConfigData.JSON_DATA_FILE_PATH, parentKey, childKey, value);
    }

    public static void updateNestedValueJsonObject(String parentKey, String childKey, Number value) {
        updateNestedJsonObject(ConfigData.JSON_DATA_FILE_PATH, parentKey, childKey, value);
    }

    /**
     * Updates a nested key in the specified file.
     */
    public static void updateNestedJsonObject(String filePath, String parentKey, String childKey,
            String value) {
        JsonObject obj = readJsonObject(filePath);
        ensureObject(obj, parentKey);
        obj.getAsJsonObject(parentKey).addProperty(childKey, value);
        writeJsonObject(filePath, obj);
    }

    public static void updateNestedJsonObject(String filePath, String parentKey, String childKey,
            Number value) {
        JsonObject obj = readJsonObject(filePath);
        ensureObject(obj, parentKey);
        obj.getAsJsonObject(parentKey).addProperty(childKey, value);
        writeJsonObject(filePath, obj);
    }

    // ═══════════════════════ UPDATE ARRAY ═══════════════════════

    /**
     * Updates a value at the given path inside a JSON array element in the default file.
     *
     * @param value the new value
     * @param index the array index
     * @param keys  path to the target key within the array element
     */
    public static void updateValueJsonArray(String value, int index, String... keys) {
        updateValueJsonArray(ConfigData.JSON_DATA_FILE_PATH, value, index, keys);
    }

    /**
     * Updates a value at the given path inside a JSON array element in the specified file.
     */
    public static void updateValueJsonArray(String filePath, String value, int index,
            String... keys) {
        if (keys == null || keys.length == 0) {
            throw new IllegalArgumentException("At least one key must be provided");
        }
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath))) {
            JsonArray jsonArray = GSON.fromJson(reader, JsonArray.class);

            if (index < 0 || index >= jsonArray.size()) {
                throw new IndexOutOfBoundsException("Index out of bounds: " + index);
            }

            JsonObject current = jsonArray.get(index).getAsJsonObject();
            for (int i = 0; i < keys.length - 1; i++) {
                ensureObject(current, keys[i]);
                current = current.getAsJsonObject(keys[i]);
            }
            current.addProperty(keys[keys.length - 1], value);

            writeJsonArray(filePath, jsonArray);
            LogUtils.info("JSON array[" + index + "] updated: "
                    + String.join(".", keys) + " = " + value);

        } catch (IOException e) {
            throw new RuntimeException("Failed to update JSON array: " + filePath, e);
        }
    }

    // ═══════════════════════ PRIVATE HELPERS ═══════════════════════

    private static JsonObject readJsonObject(String filePath) {
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath))) {
            return GSON.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + filePath, e);
        }
    }

    private static void writeJsonObject(String filePath, JsonObject obj) {
        Path path = Paths.get(filePath);
        try (OutputStream out = Files.newOutputStream(path)) {
            out.write(GSON.toJson(obj).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON file: " + filePath, e);
        }
    }

    private static void writeJsonArray(String filePath, JsonArray array) {
        Path path = Paths.get(filePath);
        try (OutputStream out = Files.newOutputStream(path)) {
            out.write(GSON.toJson(array).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON array: " + filePath, e);
        }
    }

    private static void ensureObject(JsonObject parent, String key) {
        if (!parent.has(key) || !parent.get(key).isJsonObject()) {
            parent.add(key, new JsonObject());
        }
    }

    private static void logUpdateAction(JsonObject obj, String key) {
        if (obj.has(key)) {
            LogUtils.debug("Updating existing key: " + key);
        } else {
            LogUtils.debug("Adding new key: " + key);
        }
    }
}