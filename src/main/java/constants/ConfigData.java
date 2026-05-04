package constants;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.PropertiesHelpers;
import helpers.SystemHelpers;

import java.io.File;
import java.io.IOException;

/**
 * Central access point for all framework configuration values.
 * <p>
 * Values are loaded once from {@code config.properties} via {@link PropertiesHelpers}
 * and exposed as typed constants (int / double / boolean) so callers do not need to
 * parse strings repeatedly.
 * </p>
 */
public final class ConfigData {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        PropertiesHelpers.loadAllFiles();
    }

    private ConfigData() {
        // Utility class — prevent instantiation
    }

    // ═══════════════════════ PATHS ═══════════════════════
    public static final String PROJECT_PATH = SystemHelpers.getCurrentDir();
    public static final String EXCEL_DATA_FILE_PATH = get("EXCEL_DATA_FILE_PATH");
    public static final String JSON_DATA_FILE_PATH = get("JSON_DATA_FILE_PATH");
    public static final String JSON_CONFIG_FILE_PATH = get("JSON_CONFIG_FILE_PATH");
    public static final String TEST_DATA_FOLDER_PATH = get("TEST_DATA_FOLDER_PATH");
    public static final String SCREENSHOT_PATH = get("SCREENSHOT_PATH");
    public static final String RECORD_VIDEO_PATH = get("RECORD_VIDEO_PATH");
    public static final String EXTENT_REPORT_PATH = get("EXTENT_REPORT_PATH");
    public static final String ALLURE_REPORT_PATH = get("ALLURE_REPORT_PATH");

    // ═══════════════════════ LOCALE ═══════════════════════
    public static final String LOCATE = get("LOCATE");

    // ═══════════════════════ TIMEOUTS (typed) ═══════════════════════
    public static final int TIMEOUT_SERVICE = getInt("TIMEOUT_SERVICE", 60);
    public static final int TIMEOUT_EXPLICIT_DEFAULT = getInt("TIMEOUT_EXPLICIT_DEFAULT", 10);
    public static final double STEP_ACTION_TIMEOUT = getDouble("STEP_ACTION_TIMEOUT", 1.0);

    // ═══════════════════════ FEATURE FLAGS (typed) ═══════════════════════
    public static final boolean APPIUM_DRIVER_LOCAL_SERVICE = getBool("APPIUM_DRIVER_LOCAL_SERVICE", true);
    public static final boolean SCREENSHOT_FAIL = getBool("SCREENSHOT_FAIL", true);
    public static final boolean SCREENSHOT_PASS = getBool("SCREENSHOT_PASS", false);
    public static final boolean SCREENSHOT_ALL = getBool("SCREENSHOT_ALL", false);
    public static final boolean RECORD_VIDEO = getBool("RECORD_VIDEO", false);

    // ═══════════════════════ DEVICE CAPS FROM JSON ═══════════════════════

    /**
     * Reads a device capability from {@link #JSON_CONFIG_FILE_PATH}.
     *
     * @param platform     platform name (e.g. {@code "android"}, {@code "ios"})
     * @param device       device key (e.g. {@code "pixel9"})
     * @param propertyName capability name (e.g. {@code "udid"})
     * @return the capability value, or empty string if not found
     */
    public static String getDeviceCapability(String platform, String device, String propertyName) {
        try {
            JsonNode root = MAPPER.readTree(new File(JSON_CONFIG_FILE_PATH));
            return root.path("platforms")
                    .path(platform.trim().toLowerCase())
                    .path("devices")
                    .path(device.trim().toLowerCase())
                    .path(propertyName)
                    .asText("");
        } catch (IOException e) {
            throw new RuntimeException("Failed to read device capability from JSON config", e);
        }
    }

    // ═══════════════════════ INTERNAL ═══════════════════════

    private static String get(String key) {
        return PropertiesHelpers.getValue(key);
    }

    private static int getInt(String key, int defaultValue) {
        String raw = get(key);
        try {
            return (raw == null || raw.isBlank()) ? defaultValue : Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static double getDouble(String key, double defaultValue) {
        String raw = get(key);
        try {
            return (raw == null || raw.isBlank()) ? defaultValue : Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean getBool(String key, boolean defaultValue) {
        String raw = get(key);
        return (raw == null || raw.isBlank()) ? defaultValue : Boolean.parseBoolean(raw.trim());
    }
}
