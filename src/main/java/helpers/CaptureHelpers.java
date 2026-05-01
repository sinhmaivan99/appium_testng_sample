package helpers;

import constants.ConfigData;
import drivers.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import io.appium.java_client.screenrecording.CanRecordScreen;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Base64;

/**
 * Utility for capturing screenshots and recording video on Android devices.
 */
public final class CaptureHelpers {

    private static final String SCREENSHOT_FILE_PREFIX = "screenshot_";

    private CaptureHelpers() {
        // Utility class — prevent instantiation
    }

    // ═══════════════════════ SCREENSHOT ═══════════════════════

    /**
     * Captures a screenshot and saves it to the configured screenshot directory.
     */
    public static void captureScreenshot() {
        if (DriverManager.getDriver() == null) {
            LogUtils.warn("Cannot capture screenshot — driver is null");
            return;
        }
        try {
            File srcFile = ((TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(OutputType.FILE);

            SystemHelpers.createFolder(SystemHelpers.getCurrentDir() + ConfigData.SCREENSHOT_PATH);
            String fileName = SCREENSHOT_FILE_PREFIX
                    + SystemHelpers.makeSlug(DateUtils.getCurrentDateTime()) + ".png";
            Path targetPath = Paths.get(
                    SystemHelpers.getCurrentDir() + ConfigData.SCREENSHOT_PATH, fileName);

            Files.copy(srcFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            LogUtils.info("Screenshot saved: " + targetPath.toAbsolutePath());
        } catch (IOException e) {
            LogUtils.error("Failed to save screenshot", e);
        } catch (Exception e) {
            LogUtils.error("Failed to capture screenshot: " + e.getMessage(), e);
        }
    }

    // ═══════════════════════ VIDEO RECORDING ═══════════════════════

    /**
     * Starts screen recording on the connected Android device.
     */
    public static void startRecording() {
        if (DriverManager.getDriver() == null) {
            LogUtils.warn("Cannot start recording — driver is null");
            return;
        }
        try {
            ((AndroidDriver) DriverManager.getDriver()).startRecordingScreen(
                    new AndroidStartScreenRecordingOptions()
                            .withBitRate(4_000_000)
                            .withVideoSize("1080x2400")
                            .withTimeLimit(Duration.ofMinutes(10)));
            String deviceName = String.valueOf(
                    DriverManager.getDriver().getCapabilities().getCapability("deviceName"));
            LogUtils.info("Started video recording on device: " + deviceName);
        } catch (Exception e) {
            LogUtils.error("Failed to start recording: " + e.getMessage(), e);
        }
    }

    /**
     * Stops screen recording and saves the video to the given file path.
     *
     * @param videoFilePath absolute path where the video file should be saved
     */
    public static void stopRecording(String videoFilePath) {
        if (DriverManager.getDriver() == null) {
            LogUtils.warn("Cannot stop recording — driver is null");
            return;
        }
        try {
            String base64Video = ((CanRecordScreen) ((AndroidDriver) DriverManager.getDriver()))
                    .stopRecordingScreen();

            if (base64Video == null || base64Video.isEmpty()) {
                LogUtils.warn("No video data returned from stopRecordingScreen");
                return;
            }

            byte[] videoBytes = Base64.getDecoder().decode(base64Video);
            File videoFile = new File(videoFilePath);

            // Ensure parent directory exists
            if (videoFile.getParentFile() != null) {
                videoFile.getParentFile().mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(videoFile)) {
                fos.write(videoBytes);
            }
            LogUtils.info("Video saved: " + videoFile.getAbsolutePath()
                    + " (" + videoFile.length() + " bytes)");
        } catch (Exception e) {
            LogUtils.error("Failed to stop recording: " + e.getMessage(), e);
        }
    }
}
