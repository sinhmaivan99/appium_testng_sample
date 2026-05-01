package pages;

import drivers.DriverManager;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import keywords.MobileUI;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * Base class for all Page Objects.
 * <p>
 * Initializes Appium-aware {@code @FindBy} elements via {@link AppiumFieldDecorator}.
 * Defines shared bottom navigation elements and common actions.
 * </p>
 */
public class BasePage {

    /**
     * Initializes {@link AndroidFindBy} and {@link iOSXCUITFindBy} elements.
     */
    public BasePage() {
        PageFactory.initElements(new AppiumFieldDecorator(DriverManager.getDriver()), this);
    }

    // ═══════════════════════ SHARED LOCATORS ═══════════════════════

    @AndroidFindBy(accessibility = "Date")
    @iOSXCUITFindBy(accessibility = "Date")
    protected WebElement menuDate;

    @AndroidFindBy(accessibility = "Menu")
    @iOSXCUITFindBy(accessibility = "Menu")
    protected WebElement menuMenu;

    @AndroidFindBy(accessibility = "Wallet")
    @iOSXCUITFindBy(accessibility = "Wallet")
    protected WebElement menuWallet;

    @AndroidFindBy(accessibility = "Profile")
    @iOSXCUITFindBy(accessibility = "Profile")
    protected WebElement menuProfile;

    @AndroidFindBy(accessibility = "Config")
    @iOSXCUITFindBy(accessibility = "Config")
    protected WebElement menuConfig;

    @AndroidFindBy(accessibility = "Open navigation menu")
    @iOSXCUITFindBy(accessibility = "Open navigation menu")
    protected WebElement openNavigationLeftMenu;

    @AndroidFindBy(accessibility = "Web view")
    @iOSXCUITFindBy(accessibility = "Web view")
    protected WebElement itemWebView;

    @AndroidFindBy(accessibility = "Back")
    @iOSXCUITFindBy(accessibility = "Back")
    protected WebElement buttonBack;

    // ═══════════════════════ SHARED ACTIONS ═══════════════════════

    public void clickMenuDate() {
        MobileUI.clickElement(menuDate);
    }

    public MenuPage clickMenuMenu() {
        MobileUI.clickElement(menuMenu);
        return new MenuPage();
    }

    public void clickMenuWallet() {
        MobileUI.clickElement(menuWallet);
    }

    public void clickOpenNavigationLeftMenu() {
        MobileUI.clickElement(openNavigationLeftMenu);
    }

    public void clickItemWebView() {
        MobileUI.clickElement(itemWebView);
    }

    public void clickButtonBack() {
        MobileUI.clickElement(buttonBack);
    }
}
