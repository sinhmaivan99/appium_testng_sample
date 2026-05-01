package testcases;

import commons.BaseTest;
import io.qameta.allure.*;
import listener.TestListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.LoginPage;

/**
 * Test cases for the Login screen.
 *
 * <p>Run all tests: {@code mvn test -Dsurefire.suiteXmlFiles=suites/SuiteLoginTest.xml}</p>
 * <p>View Allure report: {@code allure serve target/allure-results}</p>
 */
@Listeners(TestListener.class)
@Epic("Mobile App")
@Feature("Login")
public class LoginTest extends BaseTest {

    @Test(description = "TC01 — Login with invalid credentials should show error")
    @Story("Invalid Login")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginFailWithInvalidCredentials() {
        LoginPage loginPage = new LoginPage();
        loginPage.login("admin123", "wrongpassword");
        loginPage.verifyLoginFail();
    }

    @Test(description = "TC02 — Login with valid credentials should navigate to Menu")
    @Story("Valid Login")
    @Severity(SeverityLevel.BLOCKER)
    public void testLoginSuccess() {
        LoginPage loginPage = new LoginPage();
        loginPage.login("admin", "admin");
        loginPage.verifyLoginSuccess();
    }
}
