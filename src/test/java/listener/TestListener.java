package listener;

import constants.ConfigData;
import helpers.CaptureHelpers;
import helpers.DateUtils;
import helpers.LogUtils;
import helpers.SystemHelpers;
import keywords.MobileUI;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import reports.AllureManager;

import java.time.Duration;
import java.time.Instant;

/**
 * TestNG listener that handles per-test lifecycle events.
 * <ul>
 *     <li>Starts/stops screen recording (when {@code RECORD_VIDEO=true})</li>
 *     <li>Captures screenshots on pass/fail (per config flags)</li>
 *     <li>Attaches artifacts to the Allure report</li>
 *     <li>Logs test results with duration</li>
 * </ul>
 */
public class TestListener implements ITestListener {

    private static final String TIMESTAMP_ATTR = "startTime";
    private static final long POST_TEST_PAUSE_SECONDS = 2L;
    private static final long SKIP_PAUSE_SECONDS = 1L;

    @Override
    public void onStart(ITestContext context) {
        LogUtils.info("Suite started: " + context.getName() + " at " + context.getStartDate());
    }

    @Override
    public void onFinish(ITestContext context) {
        LogUtils.info("Suite finished: PASS=" + context.getPassedTests().size()
                + " FAIL=" + context.getFailedTests().size()
                + " SKIP=" + context.getSkippedTests().size()
                + " at " + context.getEndDate());
    }

    @Override
    public void onTestStart(ITestResult result) {
        result.setAttribute(TIMESTAMP_ATTR, Instant.now());
        LogUtils.info("Test started: " + result.getName());

        if (ConfigData.RECORD_VIDEO) {
            SystemHelpers.createFolder(SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH);
            CaptureHelpers.startRecording();
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LogUtils.info("Test PASSED: " + result.getName() + " (" + durationMs(result) + "ms)");

        if (ConfigData.SCREENSHOT_PASS) {
            CaptureHelpers.captureScreenshot();
        }
        stopRecordingIfEnabled(result.getName(), POST_TEST_PAUSE_SECONDS);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LogUtils.error("Test FAILED: " + result.getName() + " (" + durationMs(result) + "ms)");
        if (result.getThrowable() != null) {
            LogUtils.error("Cause: " + result.getThrowable());
        }

        if (ConfigData.SCREENSHOT_FAIL) {
            CaptureHelpers.captureScreenshot();
        }

        AllureManager.saveTextLog(result.getName() + " failed: " + result.getThrowable());
        AllureManager.saveScreenshotPNG();

        stopRecordingIfEnabled(result.getName(), POST_TEST_PAUSE_SECONDS);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LogUtils.warn("Test SKIPPED: " + result.getName());
        stopRecordingIfEnabled(result.getName(), SKIP_PAUSE_SECONDS);
    }

    // ═══════════════════════ INTERNAL ═══════════════════════

    private static void stopRecordingIfEnabled(String testName, long pauseSeconds) {
        if (!ConfigData.RECORD_VIDEO) return;
        MobileUI.sleep(pauseSeconds);
        CaptureHelpers.stopRecording(buildVideoFileName(testName));
    }

    private static String buildVideoFileName(String testName) {
        String timestamp = SystemHelpers.makeSlug(DateUtils.getCurrentDateTime());
        return SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH
                + "/recording_" + testName + "_" + Thread.currentThread().getId()
                + "_" + timestamp + ".mp4";
    }

    private static long durationMs(ITestResult result) {
        Instant start = (Instant) result.getAttribute(TIMESTAMP_ATTR);
        return start == null ? 0L : Duration.between(start, Instant.now()).toMillis();
    }
}
