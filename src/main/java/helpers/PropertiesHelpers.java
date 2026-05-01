package helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Utility for reading and writing {@code .properties} configuration files.
 * <p>
 * Supports loading multiple files at once via {@link #loadAllFiles()}.
 * System properties take priority over file values.
 * </p>
 */
public final class PropertiesHelpers {

    private static final Logger log = LoggerFactory.getLogger(PropertiesHelpers.class);
    private static final String DEFAULT_CONFIG_PATH =
            "src/test/resources/configs/config.properties";

    private static Properties properties = new Properties();

    private PropertiesHelpers() {
        // Utility class — prevent instantiation
    }

    // ═══════════════════════ LOAD ═══════════════════════

    /**
     * Loads all default config files. Add more paths to the list as needed.
     */
    public static void loadAllFiles() {
        List<String> files = new ArrayList<>();
        files.add("src/test/resources/configs/config.properties");
        // files.add("src/test/resources/configs/local.properties");
        // files.add("src/test/resources/configs/production.properties");

        Properties combined = new Properties();
        for (String relativePath : files) {
            String absolutePath = SystemHelpers.getCurrentDir() + relativePath;
            try (FileInputStream fis = new FileInputStream(absolutePath)) {
                Properties temp = new Properties();
                temp.load(fis);
                combined.putAll(temp);
            } catch (IOException e) {
                log.warn("Could not load properties file: {} — {}", absolutePath, e.getMessage());
            }
        }
        properties = combined;
    }

    /**
     * Loads a specific properties file, replacing currently loaded properties.
     */
    public static void setFile(String relativeFilePath) {
        String absolutePath = SystemHelpers.getCurrentDir() + relativeFilePath;
        Properties loaded = new Properties();
        try (FileInputStream fis = new FileInputStream(absolutePath)) {
            loaded.load(fis);
            properties = loaded;
        } catch (IOException e) {
            log.error("Failed to load properties file: {}", absolutePath, e);
        }
    }

    /**
     * Loads the default config file.
     */
    public static void setDefaultFile() {
        setFile(DEFAULT_CONFIG_PATH);
    }

    // ═══════════════════════ READ ═══════════════════════

    /**
     * Returns a property value. Returns {@code null} if the key is not found.
     */
    public static String getValue(String key) {
        if (properties.isEmpty()) {
            loadAllFiles();
        }
        return properties.getProperty(key);
    }

    // ═══════════════════════ WRITE ═══════════════════════

    /**
     * Writes a key-value pair to the default config file.
     */
    public static void setValue(String key, String value) {
        setValue(DEFAULT_CONFIG_PATH, key, value);
    }

    /**
     * Writes a key-value pair to the specified properties file.
     */
    public static void setValue(String relativeFilePath, String key, String value) {
        String absolutePath = SystemHelpers.getCurrentDir() + relativeFilePath;
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(absolutePath)) {
            props.load(fis);
        } catch (IOException e) {
            log.error("Failed to read properties file for update: {}", absolutePath, e);
            return;
        }

        props.setProperty(key, value);

        try (FileOutputStream fos = new FileOutputStream(absolutePath)) {
            props.store(fos, null);
            log.info("Set '{}' = '{}' in {}", key, value, relativeFilePath);
        } catch (IOException e) {
            log.error("Failed to write properties file: {}", absolutePath, e);
        }
    }

    /**
     * Removes a key from the specified properties file.
     */
    public static void removeKey(String relativeFilePath, String key) {
        String absolutePath = SystemHelpers.getCurrentDir() + relativeFilePath;
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(absolutePath)) {
            props.load(fis);
        } catch (IOException e) {
            log.error("Failed to read properties file: {}", absolutePath, e);
            return;
        }

        if (props.containsKey(key)) {
            props.remove(key);
            log.info("Removed key '{}' from {}", key, relativeFilePath);
        } else {
            log.warn("Key '{}' not found in {}", key, relativeFilePath);
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(absolutePath)) {
            props.store(fos, null);
        } catch (IOException e) {
            log.error("Failed to write updated properties file: {}", absolutePath, e);
        }
    }
}