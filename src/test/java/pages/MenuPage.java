package pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;
import keywords.MobileUI;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for the Menu (Table list) screen.
 */
public class MenuPage extends BasePage {

    // ═══════════════════════ LOCATORS ═══════════════════════

    @AndroidFindBy(xpath = "//android.widget.EditText")
    @iOSXCUITFindBy(accessibility = "")
    private WebElement inputSearch;

    @AndroidFindBy(xpath = "(//android.view.View[contains(@content-desc,\"Table\")])[1]")
    @iOSXCUITFindBy(accessibility = "")
    private WebElement firstItemTable;

    @AndroidFindBy(xpath = "//android.view.View[contains(@content-desc,\"Table\")]")
    @iOSXCUITFindBy(xpath = "")
    private List<WebElement> listItemTable;

    // ═══════════════════════ ACTIONS ═══════════════════════

    @Step("Search for table: {tableName}")
    public MenuPage searchTable(String tableName) {
        clickMenuMenu();
        MobileUI.clickElement(inputSearch);
        MobileUI.setText(inputSearch, tableName);
        return this;
    }

    @Step("Verify table count is at least: {expectedMinimum}")
    public void verifyTableResultMinimum(int expectedMinimum) {
        int actual = listItemTable.size();
        MobileUI.assertTrueCondition(actual >= expectedMinimum,
                "Expected at least " + expectedMinimum + " tables but found " + actual);
    }

    @Step("Click first table item")
    public void clickFirstItemTable() {
        MobileUI.clickElement(firstItemTable);
    }
}