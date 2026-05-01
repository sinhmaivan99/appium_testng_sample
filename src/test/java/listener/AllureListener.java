package listener;

import drivers.DriverManager;
import io.qameta.allure.Allure;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.TestResult;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;

/**
 * Allure lifecycle listener that attaches screenshots on test pass and fail.
 */
public class AllureListener implements TestLifecycleListener {

    @Override
    public void beforeTestStop(TestResult result) {
        if (DriverManager.getDriver() == null) {
            return;
        }

        Status status = result.getStatus();
        if (status == Status.PASSED || status == Status.FAILED) {
            byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver())
                    .getScreenshotAs(OutputType.BYTES);
            String label = result.getName() + "_" + status.value() + "_screenshot";
            Allure.addAttachment(label, new ByteArrayInputStream(screenshot));
        }
    }
}