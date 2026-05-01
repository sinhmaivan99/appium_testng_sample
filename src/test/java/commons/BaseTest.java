package commons;

import constants.ConfigData;
import drivers.DriverManager;
import helpers.*;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import keywords.MobileUI;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;

/**
 * Base class for all TestNG test classes.
 * <p>
 * Handles Appium driver lifecycle (init/teardown) and optional screen recording.
 * Extend this class in your test classes.
 * </p>
 *
 * <h3>TestListener can be applied in three ways:</h3>
 * <ol>
 *     <li>On each test class: {@code @Listeners(TestListener.class)}</li>
 *     <li>On this BaseTest to apply globally: {@code @Listeners({TestListener.class})}</li>
 *     <li>In the TestNG XML suite file (recommended)</li>
 * </ol>
 */
public class BaseTest {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "4723";
    private static final int APPIUM_SERVICE_TIMEOUT_SECONDS = 60;

    private AppiumDriverLocalService appiumService;
    private String currentHost = DEFAULT_HOST;
    private String currentPort = DEFAULT_PORT;
    private String videoFileName;

    // ═══════════════════════ SUITE SETUP ═══════════════════════

    @BeforeSuite
    public void cleanOutputFolders() {
        FileCleanupHelper.deleteOutputFiles();
    }

    // ═══════════════════════ METHOD SETUP ═══════════════════════

    @BeforeMethod(alwaysRun = true)
    @Parameters({"platformName", "platformVersion", "deviceName", "udid", "automationName",
            "appPackage", "appActivity", "noReset", "host", "port", "systemPort"})
    public void setUpDriver(
            @Optional String platformName,
            String platformVersion,
            String deviceName,
            @Optional String udid,
            @Optional String automationName,
            @Optional String appPackage,
            @Optional String appActivity,
            boolean noReset,
            String host,
            String port,
            @Optional String systemPort) throws MalformedURLException {

        currentHost = host;
        currentPort = port;

        startAppiumServer(host, port);

        LogUtils.info("Initializing driver: platform=" + platformName
                + " device=" + deviceName + " thread=" + Thread.currentThread().getId());

        AppiumDriver driver;
        try {
            driver = switch (platformName.trim().toLowerCase()) {
                case "android" -> createAndroidDriver(host, port, platformName, platformVersion,
                        deviceName, udid, automationName, appPackage, appActivity, noReset,
                        systemPort);
                case "ios" -> createIOSDriver(host, port, platformName, platformVersion,
                        deviceName, automationName, noReset);
                default -> throw new IllegalArgumentException("Unsupported platform: " + platformName);
            };
        } catch (Exception e) {
            String msg = "❌ Failed to initialize Appium driver for thread "
                    + Thread.currentThread().getId() + " on device " + deviceName;
            LogUtils.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        DriverManager.setDriver(driver);

        // Start video recording
        videoFileName = SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH
                + "/recording_" + deviceName + "_"
                + SystemHelpers.makeSlug(DateUtils.getCurrentDateTime()) + ".mp4";
        SystemHelpers.createFolder(SystemHelpers.getCurrentDir() + ConfigData.RECORD_VIDEO_PATH);
        CaptureHelpers.startRecording();
    }

    // ═══════════════════════ METHOD TEARDOWN ═══════════════════════

    @AfterMethod(alwaysRun = true)
    public void tearDownDriver() {
        if (DriverManager.getDriver() != null) {
            CaptureHelpers.captureScreenshot();
            MobileUI.sleep(2);
            CaptureHelpers.stopRecording(videoFileName);
            DriverManager.quitDriver();
            LogUtils.info("Driver quit for thread: " + Thread.currentThread().getId());
        }

        if ("true".equalsIgnoreCase(ConfigData.APPIUM_DRIVER_LOCAL_SERVICE.trim())) {
            stopAppiumServer();
        }
    }

    // ═══════════════════════ APPIUM SERVER ═══════════════════════

    /**
     * Starts a local Appium server on the specified host and port.
     */
    public void startAppiumServer(String host, String port) {
        LogUtils.info("Starting Appium server on " + host + ":" + port);
        SystemHelpers.killProcessOnPort(port);

        AppiumServiceBuilder builder = new AppiumServiceBuilder()
                .withIPAddress(host)
                .usingPort(Integer.parseInt(port))
                .withArgument(GeneralServerFlag.LOG_LEVEL, "info")
                .withTimeout(Duration.ofSeconds(APPIUM_SERVICE_TIMEOUT_SECONDS));

        appiumService = AppiumDriverLocalService.buildService(builder);
        appiumService.start();

        if (appiumService.isRunning()) {
            LogUtils.info("✅ Appium server started on " + host + ":" + port);
        } else {
            LogUtils.error("❌ Failed to start Appium server on " + host + ":" + port);
        }
    }

    /**
     * Stops the local Appium server if it is running.
     */
    public void stopAppiumServer() {
        if (appiumService != null && appiumService.isRunning()) {
            appiumService.stop();
            LogUtils.info("Appium server stopped on " + currentHost + ":" + currentPort);
        }
        SystemHelpers.killProcessOnPort(currentPort);
    }

    // ═══════════════════════ APP HELPER ═══════════════════════

    /**
     * Downloads demo data from the app's server database screen.
     *
     * @param dataNumber the data set number to download (1–5)
     */
    protected void downloadDataFromServer(int dataNumber) {
        DriverManager.getDriver().findElement(AppiumBy.accessibilityId("Config")).click();
        DriverManager.getDriver().findElement(AppiumBy.accessibilityId("Server database")).click();
        MobileUI.sleep(2);
        DriverManager.getDriver()
                .findElement(AppiumBy.xpath(
                        "//android.view.View[contains(@content-desc,'Data " + dataNumber
                                + "')]/android.widget.Button"))
                .click();
        DriverManager.getDriver().findElement(AppiumBy.accessibilityId("Replace")).click();
        MobileUI.sleep(1);

        boolean downloaded = DriverManager.getDriver()
                .findElement(AppiumBy.accessibilityId("Downloaded")).isDisplayed();
        if (downloaded) {
            LogUtils.info("Demo data " + dataNumber + " downloaded successfully.");
        } else {
            LogUtils.warn("Could not verify download of demo data " + dataNumber);
        }
        MobileUI.sleep(2);
        DriverManager.getDriver().findElement(AppiumBy.accessibilityId("Back")).click();
    }

    // ═══════════════════════ DRIVER FACTORIES ═══════════════════════

    private AndroidDriver createAndroidDriver(
            String host, String port,
            String platform, String version, String device,
            String udid, String automationName,
            String appPackage, String appActivity,
            boolean noReset, String systemPort) throws MalformedURLException {

        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName(platform)
                .setPlatformVersion(version)
                .setDeviceName(device)
                .setAutomationName(Objects.requireNonNullElse(automationName, "UiAutomator2"))
                .setNoReset(noReset);

        if (udid != null && !udid.isBlank()) options.setUdid(udid);
        if (appPackage != null && !appPackage.isBlank()) options.setAppPackage(appPackage);
        if (appActivity != null && !appActivity.isBlank()) options.setAppActivity(appActivity);
        if (systemPort != null && !systemPort.isBlank()) {
            options.setSystemPort(Integer.parseInt(systemPort));
        }

        LogUtils.info("Creating AndroidDriver on " + device + " (thread "
                + Thread.currentThread().getId() + ")");
        return new AndroidDriver(new URL("http://" + host + ":" + port), options);
    }

    private IOSDriver createIOSDriver(
            String host, String port,
            String platform, String version, String device,
            String automationName, boolean noReset) throws MalformedURLException {

        XCUITestOptions options = new XCUITestOptions()
                .setPlatformName(platform)
                .setPlatformVersion(version)
                .setDeviceName(device)
                .setAutomationName(Objects.requireNonNullElse(automationName, "XCUITest"))
                .setNoReset(noReset);

        LogUtils.info("Creating IOSDriver on " + device + " (thread "
                + Thread.currentThread().getId() + ")");
        return new IOSDriver(new URL("http://" + host + ":" + port), options);
    }
}
