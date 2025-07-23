package reports;

import drivers.DriverManager;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class AllureManager {
    @Attachment(value = "{0}", type = "text/plain")
    public static void saveTextLog(String message) {
    }

    @Attachment(value = "Page screenshot", type = "image/png")
    public static void saveScreenshotPNG() {
        ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
    }
}
