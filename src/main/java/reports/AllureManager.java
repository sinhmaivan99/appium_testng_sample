package reports;

import drivers.DriverManager;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/**
 * Utility for attaching artifacts to the Allure report.
 */
public final class AllureManager {

    private AllureManager() {
        // Utility class — prevent instantiation
    }

    /**
     * Attaches a plain-text message to the Allure report.
     *
     * @param message the text to attach
     * @return the message bytes for Allure to process
     */
    @Attachment(value = "{0}", type = "text/plain")
    public static String saveTextLog(String message) {
        return message;
    }

    /**
     * Captures a screenshot of the current state and attaches it to the Allure report.
     *
     * @return screenshot bytes, or {@code null} if driver is unavailable
     */
    @Attachment(value = "Page Screenshot", type = "image/png")
    public static byte[] saveScreenshotPNG() {
        if (DriverManager.getDriver() == null) {
            return null;
        }
        return ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
    }
}
