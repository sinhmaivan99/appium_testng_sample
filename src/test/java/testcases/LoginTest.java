package testcases;

import commons.BaseTest;
import listener.TestListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.LoginPage;

/*
* suite -> test -> class -> method -> tcs
* allure serve target/allure-results
* */

/*
* Có 3 cách sử dụng testListener
* 1: Sử dụng trong class testcase: @Listeners(TestListener.class)
* 2: sử dụng trong class BaseTest để apply all class testcase: @Listeners({TestListener.class})
* 3: sử dụng trong file xml runner:
* */

public class LoginTest extends BaseTest {

    //Khai báo các đối tượng Page class liên quan
    private LoginPage loginPage;

    @Test
    public void testLoginFailWithUsernameInvalid() {
        //Khởi tạo đối tượng Page class
        loginPage = new LoginPage();

        //Gọi hàm từ Page class sử dụng
        loginPage.login("admin123", "admin");
        loginPage.verifyLoginFail();
    }

    @Test
    public void testLoginSuccess() {
        //Khởi tạo đối tượng Page class
        loginPage = new LoginPage();

        //Gọi hàm từ Page class sử dụng
        loginPage.login("admin", "admin");
        loginPage.verifyLoginSuccess();
    }
}
