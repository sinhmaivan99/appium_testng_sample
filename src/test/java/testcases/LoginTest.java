package testcases;

import commons.BaseTest;
import helpers.CaptureHelpers;
import org.testng.annotations.Test;
import pages.LoginPage;

public class LoginTest extends BaseTest {

    //Khai báo các đối tượng Page class liên quan
    private LoginPage loginPage;

    @Test
    public void testLoginFailWithUsernameInvalid() {
        //Khởi tạo đối tượng Page class
        loginPage = new LoginPage();

        //Gọi hàm từ Page class sử dụng
        loginPage.login("admin123", "admin");
        CaptureHelpers.captureScreenshot("testLoginFailWithUsernameInvalid");
        loginPage.verifyLoginFail();
    }

    @Test
    public void testLoginSuccess() {
        //Khởi tạo đối tượng Page class
        loginPage = new LoginPage();

        //Gọi hàm từ Page class sử dụng
        loginPage.login("admin", "admin");
        CaptureHelpers.captureScreenshot("testLoginSuccess");
        loginPage.verifyLoginSuccess();
    }
    /*
    * suite -> test -> class -> method -> tcs
    * allure serve target/allure-results
    * */
}
