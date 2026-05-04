package helpers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Reads and writes {@code .properties} configuration files.
 * <p>
 * {@link #loadAllFiles()} loads the default config files into a single
 * combined {@link Properties} instance. Reads from {@link #getValue(String)}
 * always reflect the most recent {@link #loadAllFiles()} call.
 * </p>
 */
public final class PropertiesHelpers {

    private static final String DEFAULT_CONFIG_PATH = "src/test/resources/configs/config.properties";
    private static final List<String> DEFAULT_FILES = List.of(DEFAULT_CONFIG_PATH);

    private static volatile Properties properties = new Properties();

    private PropertiesHelpers() {
        // Utility class — prevent instantiation
    }

    // ═══════════════════════ LOAD ═══════════════════════

    /** Loads all default config files into the in-memory {@link Properties} store. */
    public static void loadAllFiles() {
        Properties combined = new Properties();
        for (String relativePath : DEFAULT_FILES) {
            String absolute = SystemHelpers.getCurrentDir() + relativePath;
            try (FileInputStream fis = new FileInputStream(absolute)) {
                Properties temp = new Properties();
                temp.load(fis);
                combined.putAll(temp);
            } catch (IOException e) {
                LogUtils.warn("Could not load properties file: " + absolute + " — " + e.getMessage());
            }
        }
        properties = combined;
    }

    /** Loads a specific properties file, replacing the in-memory store. */
    public static void setFile(String relativeFilePath) {
        String absolute = SystemHelpers.getCurrentDir() + relativeFilePath;
        Properties loaded = new Properties();
        try (FileInputStream fis = new FileInputStream(absolute)) {
            loaded.load(fis);
            properties = loaded;
        } catch (IOException e) {
            LogUtils.error("Failed to load properties file: " + absolute, e);
        }
    }

    /** Loads {@link #DEFAULT_CONFIG_PATH}. */
    public static void setDefaultFile() {
        setFile(DEFAULT_CONFIG_PATH);
    }

    // ═══════════════════════ READ ═══════════════════════

    /** Returns a property value, or {@code null} if the key is not found. */
    public static String getValue(String key) {
        if (properties.isEmpty()) {
            loadAllFiles();
        }
        return properties.getProperty(key);
    }

    // ═══════════════════════ WRITE ═══════════════════════

    /** Writes a key/value to the default config file. */
    public static void setValue(String key, String value) {
        setValue(DEFAULT_CONFIG_PATH, key, value);
    }

    /** Writes a key/value to the specified properties file. */
    public static void setValue(String relativeFilePath, String key, String value) {
        String absolute = SystemHelpers.getCurrentDir() + relativeFilePath;
        Properties props = readFile(absolute);
        if (props == null) return;
        props.setProperty(key, value);
        writeFile(absolute, props, "Set '" + key + "' = '" + value + "' in " + relativeFilePath);
    }

    /** Removes a key from the specified properties file. */
    public static void removeKey(String relativeFilePath, String key) {
        String absolute = SystemHelpers.getCurrentDir() + relativeFilePath;
        Properties props = readFile(absolute);
        if (props == null) return;
        if (!props.containsKey(key)) {
            LogUtils.warn("Key '" + key + "' not found in " + relativeFilePath);
            return;
        }
        props.remove(key);
        writeFile(absolute, props, "Removed key '" + key + "' from " + relativeFilePath);
    }

    // ═══════════════════════ INTERNAL ═══════════════════════

    private static Properties readFile(String absolutePath) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(absolutePath)) {
            props.load(fis);
            return props;
        } catch (IOException e) {
            LogUtils.error("Failed to read properties file: " + absolutePath, e);
            return null;
        }
    }

    private static void writeFile(String absolutePath, Properties props, String successMessage) {
        try (FileOutputStream fos = new FileOutputStream(absolutePath)) {
            props.store(fos, null);
            LogUtils.info(successMessage);
        } catch (IOException e) {
            LogUtils.error("Failed to write properties file: " + absolutePath, e);
        }
    }
}
