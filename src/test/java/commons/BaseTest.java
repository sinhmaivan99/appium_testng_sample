package commons;

import constants.ConfigData;
import drivers.DriverManager;
import helpers.CaptureHelpers;
import helpers.DateUtils;
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
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;

public class BaseTest {

    private AppiumDriverLocalService service;
    private String HOST = "127.0.0.1";
    private String PORT = "4723";
    private int TIMEOUT_SERVICE = 60;
    private String videoFileName;

    public void runAppiumServer(String host, String port) {
        System.out.println("host in AppiumServer: " + host);
        System.out.println("port in AppiumServer: " + port);

        HOST = host;
        PORT = port;

        SystemHelpers.killProcessOnPort(PORT);

        AppiumServiceBuilder builder = new AppiumServiceBuilder();
        builder.withIPAddress(HOST);
        builder.usingPort(Integer.parseInt(PORT));
        builder.withArgument(GeneralServerFlag.LOG_LEVEL, "info");
        builder.withTimeout(Duration.ofSeconds(TIMEOUT_SERVICE));

        service = AppiumDriverLocalService.buildService(builder);
        service.start();

        if (service.isRunning()) {
            System.out.println("##### Appium server started on " + HOST + ":" + PORT);
        } else {
            System.out.println("Failed to start Appium server.");
        }
    }

    @BeforeMethod(alwaysRun = true)
    @Parameters({"platformName", "platformVersion", "deviceName", "udid", "automationName", "appPackage", "appActivity", "noReset", "host", "port", "systemPort"})
    public void setUpDriver(@Optional String platformName, String platformVersion, String deviceName, @Optional String udid, @Optional String automationName, @Optional String appPackage, @Optional String appActivity, boolean noReset, String host, String port, @Optional String systemPort) throws MalformedURLException {
        runAppiumServer(host, port);

        System.out.println("platformName: " + platformName);
        System.out.println("platformVersion: " + platformVersion);
        System.out.println("deviceName: " + deviceName);
        System.out.println("udid: " + udid);
        System.out.println("automationName: " + automationName);
        System.out.println("appPackage: " + appPackage);
        System.out.println("appActivity: " + appActivity);
        System.out.println("noReset: " + noReset);
        System.out.println("host: " + host);
        System.out.println("port: " + port);
        System.out.println("systemPort: " + systemPort);

        AppiumDriver driver = null;

        try {
            if (platformName.equalsIgnoreCase("Android")) {
                UiAutomator2Options options = new UiAutomator2Options();
                options.setPlatformName(platformName);
                options.setPlatformVersion(platformVersion);
                options.setDeviceName(deviceName);

                if (udid != null && !udid.isEmpty()) {
                    options.setUdid(udid);
                }

                if (appPackage != null && !appPackage.isEmpty()) {
                    options.setAppPackage(appPackage);
                }

                if (appActivity != null && !appActivity.isEmpty()) {
                    options.setAppActivity(appActivity);
                }

                // options.setApp("/path/to/your/app.apk");
                options.setAutomationName(Objects.requireNonNullElse(automationName, "UiAutomator2"));
                options.setNoReset(noReset);

                if (systemPort != null && !systemPort.isEmpty()) {
                    options.setSystemPort(Integer.parseInt(systemPort));
                }

                driver = new AndroidDriver(new URL("http://" + host + ":" + port), options);
                System.out.println("Khởi tạo AndroidDriver cho thread: " + Thread.currentThread().getId() + " trên thiết bị: " + deviceName);

            } else if (platformName.equalsIgnoreCase("iOS")) {
                XCUITestOptions options = new XCUITestOptions();
                options.setPlatformName(platformName);
                options.setPlatformVersion(platformVersion);
                options.setDeviceName(deviceName);

                options.setAutomationName(Objects.requireNonNullElse(automationName, "XCUITest"));
                options.setNoReset(false);

                driver = new IOSDriver(new URL("http://" + host + ":" + port), options);
                System.out.println("Khởi tạo IOSDriver cho thread: " + Thread.currentThread().getId() + " trên thiết bị: " + deviceName);

            } else {
                throw new IllegalArgumentException("Platform không hợp lệ: " + platformName);
            }

            // Lưu driver vào ThreadLocal
            DriverManager.setDriver(driver);

            // Tạo tên file video duy nhất dựa trên device và thread
            SystemHelpers.createFolder(SystemHelpers.getCurrentDir() + "exports/videos");
            videoFileName = SystemHelpers.getCurrentDir() + "exports/videos/recording_" + deviceName + "_" + Thread.currentThread().getId() + "_" + SystemHelpers.makeSlug(DateUtils.getCurrentDateTime()) + ".mp4";
            CaptureHelpers.startRecording();

        } catch (Exception e) {
            System.err.println("❌Lỗi nghiêm trọng khi khởi tạo driver cho thread " + Thread.currentThread().getId() + " trên device " + deviceName + ": " + e.getMessage());
            // Có thể ném lại lỗi để TestNG biết test setup thất bại
            throw new RuntimeException("❌Không thể khởi tạo Appium driver ", e);
        }
    }
    
    @AfterMethod(alwaysRun = true)
    public void tearDownDriver() {
        if (DriverManager.getDriver() != null) {

            //Stop recording video
            MobileUI.sleep(2);
            CaptureHelpers.stopRecording(videoFileName);

            DriverManager.quitDriver();
            LogUtils.info("##### Driver quit and removed.");
        }

        //Dừng Appium server LOCAL nếu đã khởi động
        if (ConfigData.APPIUM_DRIVER_LOCAL_SERVICE.trim().equalsIgnoreCase("true")) {
            stopAppiumServer();
        }
    }

    //@AfterSuite
    public void stopAppiumServer() {
        if (service != null && service.isRunning()) {
            service.stop();
            System.out.println("##### Appium server stopped on " + HOST + ":" + PORT);
        }
        //Kill process on port
        SystemHelpers.killProcessOnPort(PORT);
    }

    public void downloadDataFromServer(int dataNumber) {
        //Navigate to config to download database demo
        DriverManager.getDriver().findElement(AppiumBy.accessibilityId("Config")).click();
        DriverManager.getDriver().findElement(AppiumBy.accessibilityId("Server database")).click();
        MobileUI.sleep(2);
        DriverManager.getDriver().findElement(AppiumBy.xpath("//android.view.View[contains(@content-desc,'Data " + dataNumber + "')]/android.widget.Button")).click();
        DriverManager.getDriver().findElement(AppiumBy.accessibilityId("Replace")).click();
        MobileUI.sleep(1);

        //Handle Alert Message, check displayed hoặc getText/getAttribute để kiểm tra nội dung message
        if (DriverManager.getDriver().findElement(AppiumBy.accessibilityId("Downloaded")).isDisplayed()) {
            System.out.println("Database demo downloaded.");
        } else {
            System.out.println("Warning!! Can not download Database demo.");
        }
        MobileUI.sleep(2);
        DriverManager.getDriver().findElement(AppiumBy.accessibilityId("Back")).click();
    }
}
