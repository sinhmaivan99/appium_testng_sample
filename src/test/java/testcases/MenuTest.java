package testcases;

import commons.BaseTest;
import io.qameta.allure.*;
import listener.TestListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.MenuPage;

/**
 * Test cases for the Menu (Table list) screen.
 */
@Listeners(TestListener.class)
@Epic("Mobile App")
@Feature("Menu — Table Search")
public class MenuTest extends BaseTest {

    @Test(description = "TC01 — Search for a table should return at least 2 results")
    @Story("Table Search")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchTable() {
        LoginPage loginPage = new LoginPage();
        MenuPage menuPage = loginPage.login("admin", "admin");
        loginPage.verifyLoginSuccess();

        downloadDataFromServer(4);

        menuPage.searchTable("Table 1");
        menuPage.verifyTableResultMinimum(2);
    }
}
