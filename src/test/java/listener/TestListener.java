package listener;

import constants.ConfigData;
import helpers.*;
import keywords.MobileUI;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import reports.AllureManager;

import java.time.Duration;
import java.time.Instant;

/**
 * TestNG listener that handles lifecycle events for each test method.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Starting and stopping screen recording (if enabled)</li>
 *     <li>Capturing screenshots on pass/fail (if configured)</li>
 *     <li>Attaching artifacts to Allure report</li>
 *     <li>Logging test results with duration</li>
 * </ul>
 * </p>
 */
public class TestListener implements ITestListener {

    private static final String TIMESTAMP_ATTR = "startTime";

    @Override
    public void onStart(ITestContext context) {
        FileCleanupHelper.deleteOutputFiles();
        LogUtils.info("▶ Suite started: " + context.getName()
                + " at " + context.getStartDate());
    }

    @Override
    public void onFinish(ITestContext context) {
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        LogUtils.info("🏁 Suite finished: PASS=" + passed + " FAIL=" + failed
                + " SKIP=" + skipped + " at " + context.getEndDate());
    }

    @Override
    public void onTestStart(ITestResult result) {
        result.setAttribute(TIMESTAMP_ATTR, Instant.now());
        LogUtils.info("➡ Test started: " + result.getName());

        if (ConfigData.RECORD_VIDEO.equalsIgnoreCase("true")) {
            SystemHelpers.createFolder(SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH);
            CaptureHelpers.startRecording();
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long durationMs = Duration.between(
                (Instant) result.getAttribute(TIMESTAMP_ATTR), Instant.now()).toMillis();
        LogUtils.info("✅ Test PASSED: " + result.getName() + " (" + durationMs + "ms)");

        if (ConfigData.SCREENSHOT_PASS.equalsIgnoreCase("true")) {
            CaptureHelpers.captureScreenshot();
        }

        if (ConfigData.RECORD_VIDEO.equalsIgnoreCase("true")) {
            MobileUI.sleep(2);
            CaptureHelpers.stopRecording(buildVideoFileName(result.getName()));
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        long durationMs = Duration.between(
                (Instant) result.getAttribute(TIMESTAMP_ATTR), Instant.now()).toMillis();
        LogUtils.error("❌ Test FAILED: " + result.getName() + " (" + durationMs + "ms)");
        LogUtils.error("Cause: " + result.getThrowable());

        if (ConfigData.SCREENSHOT_FAIL.equalsIgnoreCase("true")) {
            CaptureHelpers.captureScreenshot();
        }

        // Allure attachments
        AllureManager.saveTextLog(result.getName() + " failed: " + result.getThrowable());
        AllureManager.saveScreenshotPNG();

        if (ConfigData.RECORD_VIDEO.equalsIgnoreCase("true")) {
            MobileUI.sleep(2);
            CaptureHelpers.stopRecording(buildVideoFileName(result.getName()));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LogUtils.warn("⛔ Test SKIPPED: " + result.getName());

        if (ConfigData.RECORD_VIDEO.equalsIgnoreCase("true")) {
            MobileUI.sleep(1);
            CaptureHelpers.stopRecording(buildVideoFileName(result.getName()));
        }
    }

    // ═══════════════════════ PRIVATE ═══════════════════════

    private String buildVideoFileName(String testName) {
        String timestamp = SystemHelpers.makeSlug(DateUtils.getCurrentDateTime());
        return SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH
                + "/recording_" + testName + "_" + Thread.currentThread().getId()
                + "_" + timestamp + ".mp4";
    }
}