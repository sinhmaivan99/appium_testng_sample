package drivers;

import io.appium.java_client.AppiumDriver;

/**
 * Thread-safe holder for the current test thread's {@link AppiumDriver} instance.
 * <p>
 * Uses a {@link ThreadLocal} to support parallel device execution.
 * Call {@link #setDriver(AppiumDriver)} in your {@code @BeforeMethod}
 * and {@link #quitDriver()} in your {@code @AfterMethod}.
 * </p>
 */
public final class DriverManager {

    private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
        // Utility class — prevent instantiation
    }

    /**
     * Stores the driver for the current thread.
     */
    public static void setDriver(AppiumDriver driverInstance) {
        DRIVER.set(driverInstance);
    }

    /**
     * Returns the driver for the current thread, or {@code null} if not set.
     */
    public static AppiumDriver getDriver() {
        return DRIVER.get();
    }

    /**
     * Quits the driver and removes it from the ThreadLocal.
     * Safe to call even if no driver is set.
     */
    public static void quitDriver() {
        AppiumDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                DRIVER.remove();
            }
        }
    }
}
