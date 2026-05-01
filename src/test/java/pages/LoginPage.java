package pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;
import keywords.MobileUI;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the Login screen.
 * <p>
 * Provides fluent actions for entering credentials and verifying login results.
 * </p>
 */
public class LoginPage extends BasePage {

    // ═══════════════════════ LOCATORS ═══════════════════════

    @AndroidFindBy(xpath = "//android.view.View[@content-desc=\"Mobile App Flutter Beta\"]"
            + "/following-sibling::android.widget.EditText[1]")
    @iOSXCUITFindBy(accessibility = "username")
    private WebElement usernameField;

    @AndroidFindBy(xpath = "//android.view.View[@content-desc=\"Mobile App Flutter Beta\"]"
            + "/following-sibling::android.widget.EditText[2]")
    @iOSXCUITFindBy(accessibility = "password")
    private WebElement passwordField;

    @AndroidFindBy(xpath = "//android.widget.Button[@content-desc=\"Sign in\"]")
    @iOSXCUITFindBy(id = "loginBtn")
    private WebElement loginButton;

    @AndroidFindBy(accessibility = " Invalid email or password")
    @iOSXCUITFindBy(accessibility = " Invalid email or password")
    private WebElement errorMessage;

    // ═══════════════════════ ACTIONS ═══════════════════════

    /**
     * Enters credentials and taps the Sign In button.
     *
     * @param username the username to enter
     * @param password the password to enter
     * @return a new {@link MenuPage} instance (optimistic — caller should verify login)
     */
    @Step("Login with username: {username}")
    public MenuPage login(String username, String password) {
        MobileUI.clickElement(usernameField);
        MobileUI.setText(usernameField, username);
        MobileUI.clickElement(passwordField);
        MobileUI.setText(passwordField, password);
        MobileUI.clickElement(loginButton);
        return new MenuPage();
    }

    // ═══════════════════════ ASSERTIONS ═══════════════════════

    @Step("Verify login success — Menu should be displayed")
    public MenuPage verifyLoginSuccess() {
        MobileUI.verifyElementPresentAndDisplayed(menuMenu, "Menu not found after login");
        return new MenuPage();
    }

    @Step("Verify login failure — error message should be displayed")
    public void verifyLoginFail() {
        MobileUI.verifyElementPresentAndDisplayed(errorMessage,
                "Error message not displayed after failed login");
        MobileUI.verifyElementAttribute(errorMessage, "content-desc",
                " Invalid email or password",
                "Error message text does not match expected value");
    }
}