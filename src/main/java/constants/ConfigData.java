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
 * Values are loaded from {@code config.properties} via {@link PropertiesHelpers}.
 * All numeric timeouts are exposed as typed constants.
 * </p>
 */
public final class ConfigData {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ConfigData() {
        // Utility class — prevent instantiation
    }

    static {
        PropertiesHelpers.loadAllFiles();
    }

    // ═══════════════════════ PATHS ═══════════════════════
    public static final String PROJECT_PATH = SystemHelpers.getCurrentDir();
    public static final String EXCEL_DATA_FILE_PATH = PropertiesHelpers.getValue("EXCEL_DATA_FILE_PATH");
    public static final String JSON_DATA_FILE_PATH = PropertiesHelpers.getValue("JSON_DATA_FILE_PATH");
    public static final String JSON_CONFIG_FILE_PATH = PropertiesHelpers.getValue("JSON_CONFIG_FILE_PATH");
    public static final String TEST_DATA_FOLDER_PATH = PropertiesHelpers.getValue("TEST_DATA_FOLDER_PATH");
    public static final String SCREENSHOT_PATH = PropertiesHelpers.getValue("SCREENSHOT_PATH");
    public static final String RECORD_VIDEO_PATH = PropertiesHelpers.getValue("RECORD_VIDEO_PATH");
    public static final String EXTENT_REPORT_PATH = PropertiesHelpers.getValue("EXTENT_REPORT_PATH");
    public static final String ALLURE_REPORT_PATH = PropertiesHelpers.getValue("ALLURE_REPORT_PATH");

    // ═══════════════════════ TIMEOUTS ═══════════════════════
    public static final String LOCATE = PropertiesHelpers.getValue("LOCATE");
    public static final String TIMEOUT_SERVICE = PropertiesHelpers.getValue("TIMEOUT_SERVICE");
    public static final String TIMEOUT_EXPLICIT_DEFAULT = PropertiesHelpers.getValue("TIMEOUT_EXPLICIT_DEFAULT");
    public static final String STEP_ACTION_TIMEOUT = PropertiesHelpers.getValue("STEP_ACTION_TIMEOUT");

    // ═══════════════════════ FEATURE FLAGS ═══════════════════════
    public static final String APPIUM_DRIVER_LOCAL_SERVICE = PropertiesHelpers.getValue("APPIUM_DRIVER_LOCAL_SERVICE");
    public static final String SCREENSHOT_FAIL = PropertiesHelpers.getValue("SCREENSHOT_FAIL");
    public static final String SCREENSHOT_PASS = PropertiesHelpers.getValue("SCREENSHOT_PASS");
    public static final String SCREENSHOT_ALL = PropertiesHelpers.getValue("SCREENSHOT_ALL");
    public static final String RECORD_VIDEO = PropertiesHelpers.getValue("RECORD_VIDEO");

    // ═══════════════════════ DEVICE CAPS FROM JSON ═══════════════════════

    /**
     * Reads a device capability from {@code config.json}.
     *
     * @param platform     the platform name (e.g. "android", "ios")
     * @param device       the device key (e.g. "pixel6")
     * @param propertyName the capability name (e.g. "udid")
     * @return the capability value, or empty string if not found
     */
    public static String getDeviceCapability(String platform, String device, String propertyName) {
        try {
            JsonNode root = MAPPER.readTree(new File(JSON_CONFIG_FILE_PATH));
            String result = root
                    .path("platforms")
                    .path(platform.trim().toLowerCase())
                    .path("devices")
                    .path(device.trim().toLowerCase())
                    .path(propertyName)
                    .asText("");
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read device capability from JSON config", e);
        }
    }
}
