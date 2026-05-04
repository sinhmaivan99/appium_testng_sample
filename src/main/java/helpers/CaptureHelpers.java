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
 * Captures screenshots and records video on Android devices.
 */
public final class CaptureHelpers {

    private static final String SCREENSHOT_FILE_PREFIX = "screenshot_";
    private static final int RECORDING_BIT_RATE = 4_000_000;
    private static final String RECORDING_VIDEO_SIZE = "1080x2400";
    private static final Duration RECORDING_TIME_LIMIT = Duration.ofMinutes(10);

    private CaptureHelpers() {
        // Utility class — prevent instantiation
    }

    // ═══════════════════════ SCREENSHOT ═══════════════════════

    /** Captures a screenshot and saves it to {@link ConfigData#SCREENSHOT_PATH}. */
    public static void captureScreenshot() {
        if (DriverManager.getDriver() == null) {
            LogUtils.warn("Cannot capture screenshot — driver is null");
            return;
        }
        try {
            File source = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.FILE);

            String folder = SystemHelpers.getCurrentDir() + ConfigData.SCREENSHOT_PATH;
            SystemHelpers.createFolder(folder);

            String fileName = SCREENSHOT_FILE_PREFIX
                    + SystemHelpers.makeSlug(DateUtils.getCurrentDateTime()) + ".png";
            Path target = Paths.get(folder, fileName);

            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            LogUtils.info("Screenshot saved: " + target.toAbsolutePath());
        } catch (IOException e) {
            LogUtils.error("Failed to save screenshot", e);
        } catch (Exception e) {
            LogUtils.error("Failed to capture screenshot: " + e.getMessage(), e);
        }
    }

    // ═══════════════════════ VIDEO RECORDING ═══════════════════════

    /** Starts screen recording on the connected Android device. */
    public static void startRecording() {
        if (DriverManager.getDriver() == null) {
            LogUtils.warn("Cannot start recording — driver is null");
            return;
        }
        try {
            ((AndroidDriver) DriverManager.getDriver()).startRecordingScreen(
                    new AndroidStartScreenRecordingOptions()
                            .withBitRate(RECORDING_BIT_RATE)
                            .withVideoSize(RECORDING_VIDEO_SIZE)
                            .withTimeLimit(RECORDING_TIME_LIMIT));
            LogUtils.info("Started video recording on device: "
                    + DriverManager.getDriver().getCapabilities().getCapability("deviceName"));
        } catch (Exception e) {
            LogUtils.error("Failed to start recording: " + e.getMessage(), e);
        }
    }

    /**
     * Stops screen recording and saves the result to {@code videoFilePath}.
     */
    public static void stopRecording(String videoFilePath) {
        if (DriverManager.getDriver() == null) {
            LogUtils.warn("Cannot stop recording — driver is null");
            return;
        }
        try {
            String base64Video = ((CanRecordScreen) DriverManager.getDriver()).stopRecordingScreen();
            if (base64Video == null || base64Video.isEmpty()) {
                LogUtils.warn("No video data returned from stopRecordingScreen");
                return;
            }
            byte[] videoBytes = Base64.getDecoder().decode(base64Video);
            File videoFile = new File(videoFilePath);
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
