# Appium TestNG Mobile Automation Framework

Tài liệu bàn giao (Handover) dành cho **người mới** tiếp nhận dự án.
Framework kiểm thử tự động mobile (Android / iOS) xây dựng trên **Appium 2 + TestNG 7 + Java 17**, hỗ trợ **Page Object Model**, **Data-Driven Testing**, **chạy song song**, **Allure Report**.

---

## 📋 Mục lục

1. [Tổng quan kiến trúc](#-tổng-quan-kiến-trúc)
2. [Yêu cầu môi trường](#-yêu-cầu-môi-trường)
3. [Cài đặt từ đầu (Onboarding)](#-cài-đặt-từ-đầu-onboarding)
4. [Cấu trúc thư mục](#-cấu-trúc-thư-mục)
5. [Cấu hình](#-cấu-hình)
6. [Cách chạy test](#-cách-chạy-test)
7. [Cách viết test mới](#-cách-viết-test-mới)
8. [Test Data (DDT)](#-test-data-ddt)
9. [Báo cáo Allure](#-báo-cáo-allure)
10. [Logging & Debug](#-logging--debug)
11. [Troubleshooting](#-troubleshooting)
12. [Quy tắc đóng góp](#-quy-tắc-đóng-góp)
13. [Liên hệ bàn giao](#-liên-hệ-bàn-giao)

---

## 🏗️ Tổng quan kiến trúc

```
┌───────────────────────────────────────────────────────────────┐
│                       TestNG Suite (XML)                      │
│   suites/SuiteLoginTest.xml  →  truyền parameter (device,...) │
└──────────────────────────────┬────────────────────────────────┘
                               ▼
┌───────────────────────────────────────────────────────────────┐
│                BaseTest  (commons/BaseTest.java)              │
│   • @BeforeSuite : dọn folder exports/                        │
│   • @BeforeMethod: start Appium server + tạo AppiumDriver     │
│   • @AfterMethod : quit driver, stop server                   │
│   • Driver lưu trong DriverManager (ThreadLocal)              │
└──────────────────────────────┬────────────────────────────────┘
                               ▼
┌───────────────────────────────────────────────────────────────┐
│        TestCases (testcases/*.java)  → kế thừa BaseTest       │
│        Page Objects (pages/*.java)   → BasePage + PageFactory │
│        Keywords (MobileUI.java)      → action chung + @Step   │
└──────────────────────────────┬────────────────────────────────┘
                               ▼
┌───────────────────────────────────────────────────────────────┐
│   Listeners: TestListener (screenshot/video) +                │
│              AllureListener (gắn vào report)                  │
│   Reports : target/allure-results  →  allure serve            │
└───────────────────────────────────────────────────────────────┘
```

**Các điểm thiết kế quan trọng:**

| Mục | Vị trí | Ghi chú |
|---|---|---|
| Driver per-thread | [DriverManager.java](src/main/java/drivers/DriverManager.java) | `ThreadLocal<AppiumDriver>` — bắt buộc khi chạy parallel |
| Khởi tạo driver | [BaseTest.java](src/test/java/commons/BaseTest.java) | Nhận parameter từ suite XML, tự start Appium server local |
| Reusable actions | [MobileUI.java](src/main/java/keywords/MobileUI.java) | Mọi click/sendKeys phải đi qua đây để có log + `@Step` |
| Cấu hình tập trung | [config.properties](src/test/resources/configs/config.properties) → [ConfigData.java](src/main/java/constants/ConfigData.java) | Đọc 1 lần, dùng hằng số |
| Device profile | [device.json](src/test/resources/configs/device.json) | Khai báo thiết bị Android/iOS, capabilities |

---

## ✅ Yêu cầu môi trường

| Tool | Version | Biến môi trường cần set |
|------|---------|-------------------------|
| JDK | **17+** | `JAVA_HOME` |
| Maven | **3.8+** | `MAVEN_HOME`, thêm vào `PATH` |
| Node.js | 18+ | (cài Appium) |
| Appium Server | **2.x** | `npm i -g appium` |
| Appium Driver | UiAutomator2 (Android) / XCUITest (iOS) | `appium driver install uiautomator2` |
| Android SDK | mới nhất | `ANDROID_HOME`, `platform-tools` trong `PATH` |
| Allure CLI | 2.x | dùng để xem report |
| IDE | IntelliJ IDEA (khuyến nghị) | Cài plugin **Lombok** + **TestNG** |

> ⚠️ Project dùng **Lombok** → IntelliJ phải bật `Settings > Build > Compiler > Annotation Processors > Enable annotation processing`.

---

## 🚀 Cài đặt từ đầu (Onboarding)

### Bước 1. Clone & mở project
```bash
git clone <repo-url>
cd appium_testng_sample
```
Mở bằng IntelliJ → **Open project (Maven)** → đợi index xong.

### Bước 2. Cài Appium + driver
```bash
npm install -g appium
appium driver install uiautomator2
appium driver install xcuitest      # chỉ khi test iOS
appium-doctor --android              # kiểm tra môi trường
```

### Bước 3. Chuẩn bị thiết bị / emulator Android
```bash
adb devices                          # phải thấy device hoặc emulator-5554
```
- Nếu dùng emulator: mở Android Studio → Device Manager → start emulator (vd. `Pixel_9_Pro_XL`).
- Lấy đúng `udid` (cột đầu tiên của `adb devices`).

### Bước 4. Bỏ APK vào `src/test/resources/apps/`
Theo cấu hình mặc định trong [device.json](src/test/resources/configs/device.json):
```
src/test/resources/apps/taurus_flutter_app_android_release.apk
```
> Nếu app đã cài sẵn trên thiết bị, có thể bỏ qua bước này và để `noReset=true` trong suite XML.

### Bước 5. Chỉnh suite XML cho khớp máy bạn
Mở [SuiteLoginTest.xml](src/test/resources/suites/SuiteLoginTest.xml) → sửa:
```xml
<parameter name="deviceName" value="pixel9"/>
<parameter name="udid"       value="emulator-5554"/>
<parameter name="port"       value="9000"/>
```

### Bước 6. Build & smoke test
```bash
mvn clean compile                    # kiểm tra build
mvn clean test                       # chạy suite mặc định
```

### Bước 7. Xem Allure Report
```bash
allure serve target/allure-results
```

---

## 📁 Cấu trúc thư mục

```
appium_testng_sample/
├── pom.xml                           # Khai báo dependency + Surefire plugin
├── README.md
├── exports/                          # Output runtime (auto-clean trước mỗi suite)
│   ├── logs/                         # Log4j2 file logs
│   ├── screenshots/                  # Ảnh chụp khi test fail/pass
│   └── videos/                       # Screen recording (.mp4)
└── src/
    ├── main/java/
    │   ├── constants/ConfigData.java         # Hằng số đọc từ config.properties
    │   ├── drivers/DriverManager.java        # ThreadLocal AppiumDriver
    │   ├── helpers/                          # Tiện ích chung
    │   │   ├── CaptureHelpers.java           # Screenshot + video
    │   │   ├── DateUtils.java                # Format thời gian (thread-safe)
    │   │   ├── ExcelHelpers.java             # Đọc Excel (Apache POI)
    │   │   ├── FileCleanupHelper.java        # Xoá output trước test
    │   │   ├── JsonHelpers.java              # Đọc JSON (Jackson + Gson)
    │   │   ├── LogUtils.java                 # Wrapper Log4j2 (KHÔNG dùng System.out)
    │   │   ├── PropertiesHelpers.java        # Đọc .properties
    │   │   └── SystemHelpers.java            # Tiện ích OS / file
    │   ├── keywords/MobileUI.java            # Reusable actions (click/sendKey/wait...)
    │   └── reports/AllureManager.java        # Attach screenshot/video vào Allure
    └── test/
        ├── java/
        │   ├── commons/BaseTest.java         # Setup/teardown driver + Appium server
        │   ├── dataproviders/                # @DataProvider Excel/JSON/Hashtable
        │   ├── listener/
        │   │   ├── AllureListener.java       # Hook Allure lifecycle
        │   │   └── TestListener.java         # Screenshot/video/log mỗi test
        │   ├── pages/                        # Page Object Model
        │   │   ├── BasePage.java
        │   │   ├── LoginPage.java
        │   │   └── MenuPage.java
        │   └── testcases/                    # Test cases TestNG
        │       ├── LoginTest.java
        │       └── MenuTest.java
        └── resources/
            ├── apps/                         # APK / IPA
            ├── configs/
            │   ├── config.properties         # Cấu hình framework
            │   └── device.json               # Capabilities theo device
            ├── suites/                       # TestNG suite XML
            │   ├── SuiteLoginTest.xml
            │   └── SuiteMenuPage.xml
            └── test_data/                    # data.json, data.xlsx, ...
```

---

## ⚙️ Cấu hình

### 1. [config.properties](src/test/resources/configs/config.properties) — cấu hình framework

```properties
APPIUM_DRIVER_LOCAL_SERVICE = true     # true = framework tự start Appium server
TIMEOUT_SERVICE              = 60      # timeout chờ Appium server (giây)
TIMEOUT_EXPLICIT_DEFAULT     = 10      # explicit wait mặc định (giây)
STEP_ACTION_TIMEOUT          = 1       # delay trước mỗi action (giây)

SCREENSHOT_FAIL  = true                # chụp khi fail
SCREENSHOT_PASS  = true                # chụp khi pass
SCREENSHOT_ALL   = true                # chụp sau MỖI action
SCREENSHOT_PATH  = exports/screenshots
RECORD_VIDEO     = false               # bật/tắt quay video
RECORD_VIDEO_PATH= exports/videos

JSON_CONFIG_FILE_PATH = src/test/resources/configs/device.json
JSON_DATA_FILE_PATH   = src/test/resources/test_data/data.json
EXCEL_DATA_FILE_PATH  = src/test/resources/test_data/data.xlsx
```

> Khi sửa file này, không cần build lại — `ConfigData` đọc lúc runtime.

### 2. [device.json](src/test/resources/configs/device.json) — profile thiết bị

Khai báo theo `platforms.<android|ios>.devices.<key>`. Ví dụ chạy trên `pixel9`:
```xml
<parameter name="deviceName" value="pixel9"/>
```
→ Framework sẽ tra ngược ra `Pixel_9_Pro_XL`, version 16, automation `UiAutomator2`, APK path...

### 3. Suite XML — chọn test set & truyền tham số runtime

Ví dụ [SuiteLoginTest.xml](src/test/resources/suites/SuiteLoginTest.xml):
```xml
<parameter name="platformName"    value="Android"/>
<parameter name="platformVersion" value="16"/>
<parameter name="deviceName"      value="pixel9"/>
<parameter name="udid"            value="emulator-5554"/>
<parameter name="port"            value="9000"/>
<parameter name="systemPort"      value="9201"/>
<parameter name="noReset"         value="true"/>
```

---

## ▶️ Cách chạy test

### Chạy suite mặc định (cấu hình trong [pom.xml](pom.xml))
```bash
mvn clean test
```

### Chạy 1 suite cụ thể
```bash
mvn clean test -Dsurefire.suiteXmlFiles=suites/SuiteMenuPage.xml
```

### Chạy 1 test class
```bash
mvn test -Dtest=LoginTest
```

### Chạy 1 test method
```bash
mvn test -Dtest=LoginTest#testLoginSuccess
```

### Override parameter từ CLI (CI/CD)
```bash
mvn clean test -DdeviceName=pixel9 -Dport=9000 -Dudid=emulator-5554
```

### Chạy song song nhiều device

Dùng `parallel="tests"` + `thread-count`:
```xml
<suite name="Parallel" parallel="tests" thread-count="2">
    <test name="Pixel9">
        <parameter name="deviceName" value="pixel9"/>
        <parameter name="port"       value="9000"/>
        <parameter name="systemPort" value="9201"/>
        <classes><class name="testcases.LoginTest"/></classes>
    </test>
    <test name="Pixel8">
        <parameter name="deviceName" value="pixel8"/>
        <parameter name="port"       value="9100"/>
        <parameter name="systemPort" value="9202"/>
        <classes><class name="testcases.LoginTest"/></classes>
    </test>
</suite>
```
> ⚠️ Mỗi thread phải có `port` + `systemPort` riêng để không xung đột.

---

## ✏️ Cách viết test mới

### Bước 1. Tạo Page Object trong `src/test/java/pages/`

```java
public class CartPage extends BasePage {

    @AndroidFindBy(accessibility = "btn_checkout")
    private WebElement btnCheckout;

    public CartPage() {
        PageFactory.initElements(
            new AppiumFieldDecorator(DriverManager.getDriver()), this);
    }

    @Step("Bấm nút Checkout")
    public void clickCheckout() {
        MobileUI.clickElement(btnCheckout);
    }
}
```

**Lưu ý:**
- Mọi tương tác element phải đi qua [MobileUI.java](src/main/java/keywords/MobileUI.java) (đã có wait + log + `@Step`).
- KHÔNG gọi trực tiếp `element.click()` / `element.sendKeys()` trong Page.
- Dùng `@AndroidFindBy` / `@iOSXCUITFindBy` cho cross-platform.

### Bước 2. Tạo Test Class trong `src/test/java/testcases/`

```java
@Listeners(TestListener.class)
@Epic("Mobile App")
@Feature("Cart")
public class CartTest extends BaseTest {

    @Test(description = "TC01 — Checkout thành công")
    @Story("Checkout")
    @Severity(SeverityLevel.CRITICAL)
    public void testCheckoutSuccess() {
        new LoginPage().login("admin", "admin");
        new CartPage().clickCheckout();
        // assertion...
    }
}
```

### Bước 3. Khai báo class vào suite XML

```xml
<test name="Cart Test">
    <classes>
        <class name="testcases.CartTest"/>
    </classes>
</test>
```

### Bước 4. Build & chạy
```bash
mvn clean test -Dtest=CartTest
```

---

## 📊 Test Data (DDT)

### Cách 1 — JSON
```java
String username = JsonHelpers.getValueJsonObject("login", "username");
```

### Cách 2 — Excel + DataProvider
```java
@DataProvider(name = "loginData")
public Object[][] loginData() {
    return new ExcelHelpers().getExcelData(ConfigData.EXCEL_DATA_FILE_PATH, "Login");
}

@Test(dataProvider = "loginData")
public void testLogin(String username, String password) { ... }
```

### Cách 3 — Excel Hashtable (truy cập theo tên cột)
```java
@DataProvider(name = "loginHash")
public Object[][] loginHash() {
    return new ExcelHelpers().getDataHashTable(
        ConfigData.EXCEL_DATA_FILE_PATH, "Login", 2, 3);
}

@Test(dataProvider = "loginHash")
public void testLogin(Hashtable<String, String> data) {
    loginPage.login(data.get("username"), data.get("password"));
}
```

---

## 📈 Báo cáo Allure

Sau khi chạy test:
```bash
# Chạy server tạm
allure serve target/allure-results

# Hoặc generate report tĩnh
allure generate target/allure-results --clean -o target/allure-report
```

Allure tự động đính kèm:
- ✅ Pass / ❌ Fail / ⛔ Skip
- Screenshot khi fail (và pass nếu bật)
- Video MP4 (nếu `RECORD_VIDEO=true`)
- Step trace từ các `@Step` trong Page/Keyword
- Thông tin môi trường

---

## 📝 Logging & Debug

- **Logger:** [LogUtils.java](src/main/java/helpers/LogUtils.java) (wrapper của Log4j2).
- **Cấu hình:** [log4j2.xml](src/main/resources/log4j2.xml).
- **Output:** console + `exports/logs/`.
- ⚠️ KHÔNG dùng `System.out.println` — dùng:
  ```java
  LogUtils.info("..."); LogUtils.error("..."); LogUtils.warn("...");
  ```

Để debug Appium → mở terminal riêng:
```bash
appium --log-level debug
```
Rồi đặt `APPIUM_DRIVER_LOCAL_SERVICE = false` để framework dùng Appium chạy thủ công.

---

## 🔧 Troubleshooting

| Triệu chứng | Nguyên nhân | Cách fix |
|---|---|---|
| `Could not start a new session` | Appium server chưa chạy / sai port | `adb devices`, đổi `port` trong suite, hoặc bật `APPIUM_DRIVER_LOCAL_SERVICE=true` |
| `No such element` ngay khi mở app | App chưa load xong | Tăng `TIMEOUT_EXPLICIT_DEFAULT` trong [config.properties](src/test/resources/configs/config.properties) |
| `'app' option is required` | APK không tồn tại theo path trong [device.json](src/test/resources/configs/device.json) | Bỏ APK vào `src/test/resources/apps/`, hoặc set `noReset=true` nếu app đã cài |
| Lombok không nhận `@Getter`/`@Slf4j` | Annotation Processor chưa bật | IntelliJ → Settings → Build → Compiler → Annotation Processors |
| `port already in use` khi chạy parallel | 2 thread dùng chung port | Mỗi `<test>` cần `port` + `systemPort` khác nhau |
| Allure report rỗng | Chạy `mvn` ở folder khác / xoá `aspectjweaver` | Chạy ở project root, không xoá `argLine` trong [pom.xml](pom.xml) |
| Test pass nhưng không có screenshot | `SCREENSHOT_FAIL/PASS=false` | Bật trong [config.properties](src/test/resources/configs/config.properties) |
| `adb devices` không thấy emulator | ADB server bị treo | `adb kill-server && adb start-server` |

---

## 🤝 Quy tắc đóng góp

1. Tạo branch từ `main`: `feature/<ticket>-<mô-tả-ngắn>`.
2. **KHÔNG** dùng `System.out.println` → dùng `LogUtils`.
3. **KHÔNG** dùng `Thread.sleep` → dùng wait trong `MobileUI`.
4. Mọi method Page Object phải có `@Step("...")` để hiển thị trong Allure.
5. Locator viết bằng `@AndroidFindBy` / `@iOSXCUITFindBy`, không hardcode trong test class.
6. Đặt tên test: `testXxx_<điều-kiện>_<kết-quả-mong-đợi>`.
7. Trước khi tạo PR:
   ```bash
   mvn clean compile
   mvn test -Dtest=<TestVừaSửa>
   ```
8. PR phải có: ticket, file thay đổi, kết quả chạy local (screenshot Allure).

---

## 📞 Liên hệ bàn giao

| Hạng mục | Người phụ trách | Ghi chú |
|---|---|---|
| Framework core | _<điền tên>_ | DriverManager, BaseTest, MobileUI |
| Test data & device profile | _<điền tên>_ | `configs/`, `test_data/` |
| CI/CD pipeline | _<điền tên>_ | Jenkins / GitHub Actions |
| Báo cáo Allure / Dashboard | _<điền tên>_ | |

> **Khi gặp vướng mắc:** đọc lại mục [Troubleshooting](#-troubleshooting) → check log trong `exports/logs/` → screenshot/video trong `exports/` → ping người phụ trách module tương ứng.

---

_Last updated: 2026-05 — phiên bản bàn giao cho thành viên mới._
