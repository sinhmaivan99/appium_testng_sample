package commons;

import constants.ConfigData;
import drivers.DriverManager;
import helpers.FileCleanupHelper;
import helpers.LogUtils;
import helpers.SystemHelpers;
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;

/**
 * Base class for TestNG test classes.
 * <p>
 * Handles Appium server lifecycle (start/stop) and driver creation per test method.
 * Screenshots / video recording are managed by {@code listener.TestListener}.
 * </p>
 */
public class BaseTest {

    private static final String DEFAULT_AUTOMATION_ANDROID = "UiAutomator2";
    private static final String DEFAULT_AUTOMATION_IOS = "XCUITest";
    private static final String PLATFORM_ANDROID = "android";
    private static final String PLATFORM_IOS = "ios";

    /** Per-thread Appium service so parallel tests do not share state. */
    private final ThreadLocal<AppiumDriverLocalService> appiumService = new ThreadLocal<>();
    private final ThreadLocal<String> currentPort = new ThreadLocal<>();

    // ═══════════════════════ SUITE SETUP ═══════════════════════

    @BeforeSuite(alwaysRun = true)
    public void cleanOutputFolders() {
        FileCleanupHelper.deleteOutputFiles();
    }

    // ═══════════════════════ METHOD SETUP ═══════════════════════

    @BeforeMethod(alwaysRun = true)
    @Parameters({"platformName", "platformVersion", "deviceName", "udid", "automationName",
            "appPackage", "appActivity", "noReset", "host", "port", "systemPort"})
    public void setUpDriver(
            String platformName,
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

        currentPort.set(port);

        if (ConfigData.APPIUM_DRIVER_LOCAL_SERVICE) {
            startAppiumServer(host, port);
        }

        LogUtils.info("Initializing driver: platform=" + platformName
                + " device=" + deviceName + " thread=" + Thread.currentThread().getId());

        AppiumDriver driver;
        try {
            driver = switch (platformName.trim().toLowerCase()) {
                case PLATFORM_ANDROID -> createAndroidDriver(host, port, platformName, platformVersion,
                        deviceName, udid, automationName, appPackage, appActivity, noReset, systemPort);
                case PLATFORM_IOS -> createIOSDriver(host, port, platformName, platformVersion,
                        deviceName, automationName, noReset);
                default -> throw new IllegalArgumentException("Unsupported platform: " + platformName);
            };
        } catch (Exception e) {
            String msg = "Failed to initialize Appium driver for thread "
                    + Thread.currentThread().getId() + " on device " + deviceName;
            LogUtils.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        DriverManager.setDriver(driver);
    }

    // ═══════════════════════ METHOD TEARDOWN ═══════════════════════

    @AfterMethod(alwaysRun = true)
    public void tearDownDriver() {
        DriverManager.quitDriver();
        LogUtils.info("Driver quit for thread: " + Thread.currentThread().getId());

        if (ConfigData.APPIUM_DRIVER_LOCAL_SERVICE) {
            stopAppiumServer();
        }

        appiumService.remove();
        currentPort.remove();
    }

    // ═══════════════════════ APPIUM SERVER ═══════════════════════

    private void startAppiumServer(String host, String port) {
        LogUtils.info("Starting Appium server on " + host + ":" + port);
        SystemHelpers.killProcessOnPort(port);

        AppiumServiceBuilder builder = new AppiumServiceBuilder()
                .withIPAddress(host)
                .usingPort(Integer.parseInt(port))
                .withArgument(GeneralServerFlag.LOG_LEVEL, "info")
                .withTimeout(Duration.ofSeconds(ConfigData.TIMEOUT_SERVICE));

        AppiumDriverLocalService service = AppiumDriverLocalService.buildService(builder);
        service.start();
        appiumService.set(service);

        if (service.isRunning()) {
            LogUtils.info("Appium server started on " + host + ":" + port);
        } else {
            LogUtils.error("Failed to start Appium server on " + host + ":" + port);
        }
    }

    private void stopAppiumServer() {
        AppiumDriverLocalService service = appiumService.get();
        if (service != null && service.isRunning()) {
            service.stop();
            LogUtils.info("Appium server stopped on port " + currentPort.get());
        }
    }

    // ═══════════════════════ APP HELPERS ═══════════════════════

    /**
     * Downloads a demo data set from the in-app server database screen.
     *
     * @param dataNumber the data set number to download (1–5)
     */
    protected void downloadDataFromServer(int dataNumber) {
        AppiumDriver driver = DriverManager.getDriver();
        driver.findElement(AppiumBy.accessibilityId("Config")).click();
        driver.findElement(AppiumBy.accessibilityId("Server database")).click();
        MobileUI.sleep(2);
        driver.findElement(AppiumBy.xpath(
                "//android.view.View[contains(@content-desc,'Data " + dataNumber + "')]/android.widget.Button"
        )).click();
        driver.findElement(AppiumBy.accessibilityId("Replace")).click();
        MobileUI.sleep(1);

        boolean downloaded = driver.findElement(AppiumBy.accessibilityId("Downloaded")).isDisplayed();
        if (downloaded) {
            LogUtils.info("Demo data " + dataNumber + " downloaded successfully");
        } else {
            LogUtils.warn("Could not verify download of demo data " + dataNumber);
        }
        MobileUI.sleep(2);
        driver.findElement(AppiumBy.accessibilityId("Back")).click();
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
                .setAutomationName(Objects.requireNonNullElse(automationName, DEFAULT_AUTOMATION_ANDROID))
                .setNoReset(noReset);

        if (isNotBlank(udid)) options.setUdid(udid);
        if (isNotBlank(appPackage)) options.setAppPackage(appPackage);
        if (isNotBlank(appActivity)) options.setAppActivity(appActivity);
        if (isNotBlank(systemPort)) options.setSystemPort(Integer.parseInt(systemPort));

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
                .setAutomationName(Objects.requireNonNullElse(automationName, DEFAULT_AUTOMATION_IOS))
                .setNoReset(noReset);

        LogUtils.info("Creating IOSDriver on " + device + " (thread "
                + Thread.currentThread().getId() + ")");
        return new IOSDriver(new URL("http://" + host + ":" + port), options);
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
