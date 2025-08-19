package listener;

import constants.ConfigData;
import helpers.*;
import keywords.MobileUI;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import reports.AllureManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestListener implements ITestListener {

    @Override
    public void onStart(ITestContext result) {
        DeleteFilesInMultipleFolders.deleteFilesInFolder();
        LogUtils.info("♻\uFE0F Setup môi trường: " + result.getStartDate());
    }

    @Override
    public void onFinish(ITestContext result) {
        LogUtils.info("\uD83D\uDD06 Kết thúc chạy test: " + result.getEndDate());
    }

    @Override
    public void onTestStart(ITestResult result) {
        LogUtils.info("➡\uFE0F Bắt đầu chạy test case: " + result.getName());

        // Tạo tên file video duy nhất dựa trên device và thread
        SystemHelpers.createFolder(SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH);
        String videoFileName = SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH + "/recording_" + "_" + Thread.currentThread().getId() + "_" + SystemHelpers.makeSlug(DateUtils.getCurrentDateTime()) + ".mp4";

        if(ConfigData.RECORD_VIDEO.equalsIgnoreCase("true")) {
            CaptureHelpers.startRecording();
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LogUtils.info("✅ Test case " + result.getName() + " is passed.");

        LocalDateTime now = LocalDateTime.now(); // lấy ngày giờ hiện tại
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = now.format(formatter);
        LogUtils.info("Thời gian: " + formattedDate);

        if (ConfigData.SCREENSHOT_PASS.equalsIgnoreCase("true")) {
            CaptureHelpers.captureScreenshot();
        }

        SystemHelpers.createFolder(SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH);
        String videoFileName = SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH + "/recording_" + result.getName() + "_" + Thread.currentThread().getId() + "_" + SystemHelpers.makeSlug(DateUtils.getCurrentDateTime()) + ".mp4";
        MobileUI.sleep(5);

        if (ConfigData.RECORD_VIDEO.equalsIgnoreCase("true")) {
            MobileUI.sleep(2);
            CaptureHelpers.stopRecording(videoFileName);
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LogUtils.error("❌ Test case " + result.getName() + " is failed.");

        LocalDateTime now = LocalDateTime.now(); // lấy ngày giờ hiện tại
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = now.format(formatter);
        LogUtils.info("Nguyên nhân lỗi: " + result.getThrowable());

        if (ConfigData.SCREENSHOT_FAIL.equalsIgnoreCase("true")) {
            CaptureHelpers.captureScreenshot();
        }
        CaptureHelpers.captureScreenshot();

        SystemHelpers.createFolder(SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH);
        String videoFileName = SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH + "/recording_" + result.getName() + "_" + Thread.currentThread().getId() + "_" + SystemHelpers.makeSlug(DateUtils.getCurrentDateTime()) + ".mp4";

        if (ConfigData.RECORD_VIDEO.equalsIgnoreCase("true")) {
            MobileUI.sleep(2);
            CaptureHelpers.stopRecording(videoFileName);
        }

        //Add screenshot to Allure report
        AllureManager.saveTextLog(result.getName() + " is failed.");
        AllureManager.saveScreenshotPNG();

        //Connect Jira
        //Create new issue on Jira
        //Ghi logs vào file
        //Xuất report html nhìn trực quan và đẹp mắt
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LogUtils.info("⛔\uFE0F Test case " + result.getName() + " is skipped.");

        SystemHelpers.createFolder(SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH);
        String videoFileName = SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH + "/recording_" + result.getName() + "_" + Thread.currentThread().getId() + "_" + SystemHelpers.makeSlug(DateUtils.getCurrentDateTime()) + ".mp4";
        MobileUI.sleep(2);
        CaptureHelpers.stopRecording(videoFileName);
    }
}