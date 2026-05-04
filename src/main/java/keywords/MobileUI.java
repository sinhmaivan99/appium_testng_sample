package keywords;

import constants.ConfigData;
import helpers.LogUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import reports.AllureManager;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static drivers.DriverManager.getDriver;

/**
 * High-level mobile UI keyword library.
 * <p>
 * Wraps common Appium / Selenium interactions with Allure {@link Step} annotations,
 * automatic logging, and unified explicit-wait handling.
 * </p>
 */
public final class MobileUI {

    private static final String LOG_PREFIX = "[MobileUI] ";
    private static final int DEFAULT_TIMEOUT = ConfigData.TIMEOUT_EXPLICIT_DEFAULT;
    private static final double STEP_ACTION_TIMEOUT = ConfigData.STEP_ACTION_TIMEOUT;
    private static final int DEFAULT_TAP_PAUSE_MS = 200;
    private static final int DEFAULT_LONG_TAP_MS = 500;
    private static final int DEFAULT_SWIPE_DURATION_MS = 200;

    private MobileUI() {
        // Utility class — prevent instantiation
    }

    // ═══════════════════════ TIMING ═══════════════════════

    /**
     * Pauses the current thread for {@code seconds}.
     *
     * @param seconds wait duration; values &le; 0 are ignored
     */
    public static void sleep(double seconds) {
        if (seconds <= 0) return;
        log("Sleeping for " + seconds + " seconds");
        try {
            Thread.sleep((long) (1000 * seconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interrupted", e);
        }
    }

    // ═══════════════════════ GESTURES ═══════════════════════

    public static void swipe(int startX, int startY, int endX, int endY, int durationMillis) {
        stepDelay();
        log("Swipe (" + startX + "," + startY + ") -> (" + endX + "," + endY
                + ") in " + durationMillis + "ms");
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(durationMillis),
                        PointerInput.Origin.viewport(), endX, endY))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        getDriver().perform(Collections.singletonList(sequence));
    }

    public static void swipeLeft() {
        Dimension size = getDriver().manage().window().getSize();
        int y = (int) (size.height * 0.3);
        swipe((int) (size.width * 0.8), y, (int) (size.width * 0.2), y, DEFAULT_SWIPE_DURATION_MS);
    }

    public static void swipeRight() {
        Dimension size = getDriver().manage().window().getSize();
        int y = (int) (size.height * 0.3);
        swipe((int) (size.width * 0.2), y, (int) (size.width * 0.8), y, DEFAULT_SWIPE_DURATION_MS);
    }

    public static void scroll(int startX, int startY, int endX, int endY, int durationMillis) {
        swipe(startX, startY, endX, endY, durationMillis);
    }

    /** Android-specific scroll gesture using mobile: scrollGesture. */
    public static void scrollGestureCommand() {
        stepDelay();
        Map<String, Object> params = new HashMap<>();
        params.put("left", 670);
        params.put("top", 500);
        params.put("width", 200);
        params.put("height", 2000);
        params.put("direction", "down");
        params.put("percent", 1);
        log("scrollGesture: " + params);
        getDriver().executeScript("mobile: scrollGesture", params);
    }

    public static void tap(WebElement element) {
        stepDelay();
        log("Tap on element: " + element);
        Point center = centerOf(element);
        performTap(center.getX(), center.getY(), DEFAULT_LONG_TAP_MS);
    }

    public static void tap(int x, int y) {
        tap(x, y, DEFAULT_TAP_PAUSE_MS);
    }

    public static void tap(int x, int y, int durationMillis) {
        stepDelay();
        log("Tap at (" + x + "," + y + ") for " + durationMillis + "ms");
        performTap(x, y, durationMillis);
    }

    public static void zoom(WebElement element, double scale) {
        stepDelay();
        log("Zoom element=" + element + " scale=" + scale);
        Point center = centerOf(element);
        int initialDistance = 100;
        int targetDistance = (int) (initialDistance * scale);
        int steps = 10;
        int moveDuration = 50;

        PointerInput finger1 = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
        PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");

        Sequence s1 = new Sequence(finger1, 1)
                .addAction(finger1.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                        center.getX() - initialDistance, center.getY()))
                .addAction(finger1.createPointerDown(0));
        Sequence s2 = new Sequence(finger2, 1)
                .addAction(finger2.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                        center.getX() + initialDistance, center.getY()))
                .addAction(finger2.createPointerDown(0));

        for (int i = 1; i <= steps; i++) {
            int x1 = center.getX() - initialDistance
                    + (initialDistance - targetDistance) * i / steps;
            int x2 = center.getX() + initialDistance
                    + (targetDistance - initialDistance) * i / steps;
            s1.addAction(finger1.createPointerMove(Duration.ofMillis(moveDuration),
                    PointerInput.Origin.viewport(), x1, center.getY()));
            s2.addAction(finger2.createPointerMove(Duration.ofMillis(moveDuration),
                    PointerInput.Origin.viewport(), x2, center.getY()));
        }
        s1.addAction(finger1.createPointerUp(0));
        s2.addAction(finger2.createPointerUp(0));
        getDriver().perform(List.of(s1, s2));
    }

    // ═══════════════════════ CLICK ═══════════════════════

    @Step("Click element {0}")
    public static void clickElement(By locator) {
        clickElement(locator, DEFAULT_TIMEOUT);
    }

    @Step("Click element {0} within {1}s")
    public static void clickElement(By locator, int timeoutSeconds) {
        stepDelay();
        log("Click locator=" + locator + " (timeout=" + timeoutSeconds + "s)");
        waitForElementToBeClickable(locator, timeoutSeconds).click();
    }

    @Step("Click element")
    public static void clickElement(WebElement element) {
        clickElement(element, DEFAULT_TIMEOUT);
    }

    @Step("Click element within {1}s")
    public static void clickElement(WebElement element, int timeoutSeconds) {
        stepDelay();
        log("Click element=" + element + " (timeout=" + timeoutSeconds + "s)");
        waitForElementToBeClickable(element, timeoutSeconds).click();
    }

    // ═══════════════════════ TEXT INPUT ═══════════════════════

    @Step("Set text '{1}' on element {0}")
    public static void setText(By locator, String text) {
        setText(locator, text, DEFAULT_TIMEOUT);
    }

    @Step("Set text '{1}' on element {0} within {2}s")
    public static void setText(By locator, String text, int timeoutSeconds) {
        stepDelay();
        log("Set text='" + text + "' locator=" + locator);
        WebElement el = waitForElementVisible(locator, timeoutSeconds);
        el.click();
        el.clear();
        el.sendKeys(text);
    }

    @Step("Set text '{1}' on element")
    public static void setText(WebElement element, String text) {
        setText(element, text, DEFAULT_TIMEOUT);
    }

    @Step("Set text '{1}' on element within {2}s")
    public static void setText(WebElement element, String text, int timeoutSeconds) {
        stepDelay();
        log("Set text='" + text + "' element=" + element);
        WebElement el = waitForElementVisible(element, timeoutSeconds);
        el.click();
        el.clear();
        el.sendKeys(text);
    }

    @Step("Clear text on element {0}")
    public static void clearText(By locator) {
        clearText(locator, DEFAULT_TIMEOUT);
    }

    @Step("Clear text on element {0} within {1}s")
    public static void clearText(By locator, int timeoutSeconds) {
        stepDelay();
        log("Clear text locator=" + locator);
        WebElement el = waitForElementVisible(locator, timeoutSeconds);
        el.click();
        el.clear();
    }

    @Step("Clear text on element")
    public static void clearText(WebElement element) {
        clearText(element, DEFAULT_TIMEOUT);
    }

    @Step("Clear text on element within {1}s")
    public static void clearText(WebElement element, int timeoutSeconds) {
        stepDelay();
        log("Clear text element=" + element);
        WebElement el = waitForElementVisible(element, timeoutSeconds);
        el.click();
        el.clear();
    }

    // ═══════════════════════ READ ═══════════════════════

    @Step("Get text from element {0}")
    public static String getElementText(By locator) {
        return getElementText(locator, DEFAULT_TIMEOUT);
    }

    @Step("Get text from element {0} within {1}s")
    public static String getElementText(By locator, int timeoutSeconds) {
        stepDelay();
        String text = waitForElementVisible(locator, timeoutSeconds).getText();
        log("Text from " + locator + " = '" + text + "'");
        AllureManager.saveTextLog("➡\uFE0F TEXT: " + text);
        return text;
    }

    public static String getElementText(WebElement element) {
        return getElementText(element, DEFAULT_TIMEOUT);
    }

    public static String getElementText(WebElement element, int timeoutSeconds) {
        stepDelay();
        String text = waitForElementVisible(element, timeoutSeconds).getText();
        log("Text = '" + text + "'");
        return text;
    }

    public static String getElementAttribute(By locator, String attribute) {
        return getElementAttribute(locator, attribute, DEFAULT_TIMEOUT);
    }

    public static String getElementAttribute(By locator, String attribute, int timeoutSeconds) {
        stepDelay();
        String value = waitForElementVisible(locator, timeoutSeconds).getAttribute(attribute);
        log("Attribute '" + attribute + "' on " + locator + " = '" + value + "'");
        return value;
    }

    public static String getElementAttribute(WebElement element, String attribute) {
        return getElementAttribute(element, attribute, DEFAULT_TIMEOUT);
    }

    public static String getElementAttribute(WebElement element, String attribute, int timeoutSeconds) {
        stepDelay();
        String value = waitForElementVisible(element, timeoutSeconds).getAttribute(attribute);
        log("Attribute '" + attribute + "' = '" + value + "'");
        return value;
    }

    // ═══════════════════════ STATE CHECKS ═══════════════════════

    public static boolean isElementPresentAndDisplayed(WebElement element) {
        try {
            return element != null && element.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        } catch (Exception e) {
            log("isElementPresentAndDisplayed error: " + e.getMessage());
            return false;
        }
    }

    public static boolean isElementPresentAndDisplayed(By locator) {
        try {
            return getDriver().findElement(locator).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        } catch (Exception e) {
            log("isElementPresentAndDisplayed error: " + e.getMessage());
            return false;
        }
    }

    public static boolean isElementEnabled(WebElement element) {
        try {
            return element != null && element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isElementEnabled(By locator) {
        try {
            return waitForElementVisible(locator).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isElementSelected(WebElement element) {
        try {
            return element != null && element.isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isElementSelected(By locator) {
        try {
            return waitForElementVisible(locator).isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    // ═══════════════════════ VERIFICATIONS ═══════════════════════

    public static void verifyElementPresentAndDisplayed(WebElement element, String message) {
        Assert.assertTrue(isElementPresentAndDisplayed(element), message);
    }

    public static void verifyElementPresentAndDisplayed(By locator, String message) {
        Assert.assertTrue(isElementPresentAndDisplayed(locator), message);
    }

    public static void verifyElementEnabled(WebElement element, String message) {
        Assert.assertTrue(isElementEnabled(element), message);
    }

    public static void verifyElementEnabled(By locator, String message) {
        Assert.assertTrue(isElementEnabled(locator), message);
    }

    public static void verifyElementSelected(WebElement element, String message) {
        Assert.assertTrue(isElementSelected(element), message);
    }

    public static void verifyElementSelected(By locator, String message) {
        Assert.assertTrue(isElementSelected(locator), message);
    }

    public static void verifyElementText(WebElement element, String expected, String message) {
        Assert.assertEquals(getElementText(element), expected, message);
    }

    public static void verifyElementText(By locator, String expected, String message) {
        Assert.assertEquals(getElementText(locator), expected, message);
    }

    public static void verifyElementAttribute(WebElement element, String attribute,
                                              String expected, String message) {
        Assert.assertEquals(getElementAttribute(element, attribute), expected, message);
    }

    public static void verifyElementAttribute(By locator, String attribute,
                                              String expected, String message) {
        Assert.assertEquals(getElementAttribute(locator, attribute), expected, message);
    }

    public static void assertTrueCondition(boolean condition, String message) {
        Assert.assertTrue(condition, message);
    }

    // ═══════════════════════ EXPLICIT WAITS ═══════════════════════

    public static WebElement waitForElementToBeClickable(By locator) {
        return waitForElementToBeClickable(locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForElementToBeClickable(By locator, int timeoutSeconds) {
        return waitFor(ExpectedConditions.elementToBeClickable(locator), timeoutSeconds,
                "clickable: " + locator);
    }

    public static WebElement waitForElementToBeClickable(WebElement element) {
        return waitForElementToBeClickable(element, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForElementToBeClickable(WebElement element, int timeoutSeconds) {
        return waitFor(ExpectedConditions.elementToBeClickable(element), timeoutSeconds,
                "clickable: " + element);
    }

    public static WebElement waitForElementVisible(By locator) {
        return waitForElementVisible(locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForElementVisible(By locator, int timeoutSeconds) {
        return waitFor(ExpectedConditions.visibilityOfElementLocated(locator), timeoutSeconds,
                "visible: " + locator);
    }

    public static WebElement waitForElementVisible(WebElement element) {
        return waitForElementVisible(element, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForElementVisible(WebElement element, int timeoutSeconds) {
        return waitFor(ExpectedConditions.visibilityOf(element), timeoutSeconds,
                "visible: " + element);
    }

    public static boolean waitForElementInvisible(By locator) {
        return waitForElementInvisible(locator, DEFAULT_TIMEOUT);
    }

    public static boolean waitForElementInvisible(By locator, int timeoutSeconds) {
        return waitFor(ExpectedConditions.invisibilityOfElementLocated(locator), timeoutSeconds,
                "invisible: " + locator);
    }

    public static boolean waitForElementInvisible(WebElement element) {
        return waitForElementInvisible(element, DEFAULT_TIMEOUT);
    }

    public static boolean waitForElementInvisible(WebElement element, int timeoutSeconds) {
        return waitFor(ExpectedConditions.invisibilityOf(element), timeoutSeconds,
                "invisible: " + element);
    }

    public static WebElement waitForElementPresent(By locator) {
        return waitForElementPresent(locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForElementPresent(By locator, int timeoutSeconds) {
        return waitFor(ExpectedConditions.presenceOfElementLocated(locator), timeoutSeconds,
                "present: " + locator);
    }

    public static boolean waitForTextToBePresent(By locator, String text) {
        return waitForTextToBePresent(locator, text, DEFAULT_TIMEOUT);
    }

    public static boolean waitForTextToBePresent(By locator, String text, int timeoutSeconds) {
        return waitFor(ExpectedConditions.textToBePresentInElementLocated(locator, text),
                timeoutSeconds, "text '" + text + "' in " + locator);
    }

    public static boolean waitForTextToBePresent(WebElement element, String text) {
        return waitForTextToBePresent(element, text, DEFAULT_TIMEOUT);
    }

    public static boolean waitForTextToBePresent(WebElement element, String text, int timeoutSeconds) {
        return waitFor(ExpectedConditions.textToBePresentInElement(element, text),
                timeoutSeconds, "text '" + text + "'");
    }

    public static boolean waitForAttributeToBe(By locator, String attribute, String value) {
        return waitForAttributeToBe(locator, attribute, value, DEFAULT_TIMEOUT);
    }

    public static boolean waitForAttributeToBe(By locator, String attribute, String value, int timeoutSeconds) {
        return waitFor(ExpectedConditions.attributeToBe(locator, attribute, value),
                timeoutSeconds, "attribute " + attribute + "='" + value + "'");
    }

    public static boolean waitForAttributeToBe(WebElement element, String attribute, String value) {
        return waitForAttributeToBe(element, attribute, value, DEFAULT_TIMEOUT);
    }

    public static boolean waitForAttributeToBe(WebElement element, String attribute, String value, int timeoutSeconds) {
        return waitFor(ExpectedConditions.attributeToBe(element, attribute, value),
                timeoutSeconds, "attribute " + attribute + "='" + value + "'");
    }

    public static List<WebElement> waitForNumberOfElements(By locator, int expectedCount) {
        return waitForNumberOfElements(locator, expectedCount, DEFAULT_TIMEOUT);
    }

    public static List<WebElement> waitForNumberOfElements(By locator, int expectedCount, int timeoutSeconds) {
        return waitFor(ExpectedConditions.numberOfElementsToBe(locator, expectedCount),
                timeoutSeconds, "count=" + expectedCount + " for " + locator);
    }

    public static boolean waitForUrlContains(String text) {
        return waitForUrlContains(text, DEFAULT_TIMEOUT);
    }

    public static boolean waitForUrlContains(String text, int timeoutSeconds) {
        return waitFor(ExpectedConditions.urlContains(text), timeoutSeconds, "URL contains '" + text + "'");
    }

    public static boolean waitForNumberOfWindows(int expectedWindows) {
        return waitForNumberOfWindows(expectedWindows, DEFAULT_TIMEOUT);
    }

    public static boolean waitForNumberOfWindows(int expectedWindows, int timeoutSeconds) {
        return waitFor(ExpectedConditions.numberOfWindowsToBe(expectedWindows),
                timeoutSeconds, "windows=" + expectedWindows);
    }

    // ═══════════════════════ INTERNAL ═══════════════════════

    private static <T> T waitFor(ExpectedCondition<T> condition, int timeoutSeconds, String description) {
        log("Waiting up to " + timeoutSeconds + "s for " + description);
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds)).until(condition);
    }

    private static void stepDelay() {
        sleep(STEP_ACTION_TIMEOUT);
    }

    private static void log(String message) {
        LogUtils.info(LOG_PREFIX + message);
    }

    private static Point centerOf(WebElement element) {
        Point loc = element.getLocation();
        Dimension size = element.getSize();
        return new Point(loc.getX() + size.getWidth() / 2, loc.getY() + size.getHeight() / 2);
    }

    private static void performTap(int x, int y, int holdDurationMs) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence sequence = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, Duration.ofMillis(holdDurationMs)))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        getDriver().perform(Collections.singletonList(sequence));
    }
}
