# Appium TestNG Mobile Automation Framework

A professional, enterprise-grade mobile test automation framework built with **Appium 2**, **TestNG 7**, and **Java 17**. Supports Android and iOS parallel execution, data-driven testing, and Allure reporting.

---

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [How to Run Tests](#how-to-run-tests)
- [Test Data](#test-data)
- [Reporting](#reporting)
- [Design Patterns](#design-patterns)
- [Contributing](#contributing)

---

## ✅ Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java (JDK) | 17+ | Set `JAVA_HOME` |
| Maven | 3.8+ | Set `MAVEN_HOME` |
| Node.js | 18+ | Required for Appium |
| Appium | 2.x | `npm install -g appium` |
| Android SDK | Latest | Set `ANDROID_HOME` |
| Appium Driver | UiAutomator2 | `appium driver install uiautomator2` |
| Allure CLI | 2.x | For report generation |

---

## 📁 Project Structure

```
appium_testng_sample/
├── src/
│   ├── main/java/
│   │   ├── constants/
│   │   │   └── ConfigData.java          # Central config constants
│   │   ├── drivers/
│   │   │   └── DriverManager.java       # Thread-safe driver holder (ThreadLocal)
│   │   ├── helpers/
│   │   │   ├── CaptureHelpers.java      # Screenshot & video recording
│   │   │   ├── DateUtils.java           # Thread-safe date/time utilities
│   │   │   ├── DeleteFilesInMultipleFolders.java  # Legacy (replaced by FileCleanupHelper)
│   │   │   ├── ExcelHelpers.java        # Excel data reading/writing (Apache POI)
│   │   │   ├── FileCleanupHelper.java   # Output folder cleanup
│   │   │   ├── JsonHelpers.java         # JSON read/write (Jackson + Gson)
│   │   │   ├── LogUtils.java            # Caller-aware Log4j2 wrapper
│   │   │   ├── PropertiesHelpers.java   # .properties file reader/writer
│   │   │   └── SystemHelpers.java       # OS/filesystem utilities
│   │   ├── keywords/
│   │   │   └── MobileUI.java            # Reusable mobile interactions (Allure @Step)
│   │   └── reports/
│   │       └── AllureManager.java       # Allure attachment utilities
│   └── test/
│       ├── java/
│       │   ├── commons/
│       │   │   └── BaseTest.java        # Driver init, teardown, Appium server
│       │   ├── dataproviders/
│       │   │   └── DataProviderFactory.java  # TestNG @DataProvider methods
│       │   ├── listener/
│       │   │   ├── AllureListener.java  # Allure lifecycle screenshots
│       │   │   └── TestListener.java    # Video, screenshot, logging per test
│       │   ├── pages/
│       │   │   ├── BasePage.java        # Shared locators + PageFactory init
│       │   │   ├── LoginPage.java       # Login screen Page Object
│       │   │   └── MenuPage.java        # Menu/Table screen Page Object
│       │   └── testcases/
│       │       ├── LoginTest.java       # Login test scenarios
│       │       └── MenuTest.java        # Menu test scenarios
│       └── resources/
│           ├── apps/                    # APK / IPA files
│           ├── configs/
│           │   └── config.properties    # Framework configuration
│           ├── suites/
│           │   └── SuiteLoginTest.xml   # TestNG suite definitions
│           └── test_data/               # Excel and JSON test data files
├── pom.xml
└── README.md
```

---

## 🚀 Quick Start

### 1. Install Appium and drivers

```bash
npm install -g appium
appium driver install uiautomator2
appium driver install xcuitest   # iOS only
```

### 2. Connect a device

```bash
adb devices    # Verify Android device is visible
```

### 3. Configure the device in `SuiteLoginTest.xml`

```xml
<parameter name="deviceName" value="YOUR_DEVICE_NAME"/>
<parameter name="udid" value="YOUR_DEVICE_UDID"/>
```

### 4. Run tests

```bash
mvn clean test
```

### 5. View Allure report

```bash
allure serve target/allure-results
```

---

## ⚙️ Configuration

All settings are in `src/test/resources/configs/config.properties`:

```properties
# ─── Timeouts ────────────────────────────────
TIMEOUT_EXPLICIT_DEFAULT=30       # seconds for element waits
STEP_ACTION_TIMEOUT=500           # ms pause before each action
TIMEOUT_SERVICE=60                # Appium server startup timeout

# ─── Screenshot & Video ──────────────────────
SCREENSHOT_FAIL=true              # capture on test failure
SCREENSHOT_PASS=false             # capture on test pass
SCREENSHOT_ALL=false              # capture after every action
SCREENSHOT_PATH=exports/screenshots
RECORD_VIDEO=true                 # enable screen recording
RECORD_VIDEO_PATH=exports/videos

# ─── Appium ──────────────────────────────────
APPIUM_DRIVER_LOCAL_SERVICE=true  # auto-start local Appium server

# ─── Data Paths ──────────────────────────────
EXCEL_DATA_FILE_PATH=src/test/resources/test_data/data.xlsx
JSON_DATA_FILE_PATH=src/test/resources/test_data/data.json
JSON_CONFIG_FILE_PATH=src/test/resources/configs/config.json
```

---

## ▶️ How to Run Tests

### Run default suite
```bash
mvn clean test
```

### Run with a specific suite
```bash
mvn clean test -Dsurefire.suiteXmlFiles=suites/SuiteMenuTest.xml
```

### Run a specific test class
```bash
mvn test -Dtest=LoginTest
```

### Pass parameters from CLI (CI/CD)
```bash
mvn clean test -DdeviceName="Pixel 6" -Dport="4724"
```

---

## 📊 Test Data

### Excel DDT (DataProvider)

```java
@DataProvider(name = "login_from_excel")
public Object[][] loginFromExcel() {
    return new ExcelHelpers().getExcelData(
            ConfigData.EXCEL_DATA_FILE_PATH, "Login");
}
```

### JSON DDT

```java
String username = JsonHelpers.getValueJsonObject("login", "username");
```

### Hashtable DDT (map column name → value)

```java
@DataProvider(name = "login_from_excel_hashtable")
public Object[][] loginHashtable() {
    return new ExcelHelpers().getDataHashTable(
            ConfigData.EXCEL_DATA_FILE_PATH, "Login", 2, 3);
}
```

In test:
```java
@Test(dataProvider = "login_from_excel_hashtable")
public void testLogin(Hashtable<String, String> data) {
    loginPage.login(data.get("username"), data.get("password"));
}
```

---

## 📈 Reporting

### Allure Report

After running tests:

```bash
# Serve report locally
allure serve target/allure-results

# Or generate static report
allure generate target/allure-results --clean -o target/allure-report
```

Reports include:
- ✅ Pass / ❌ Fail / ⛔ Skip counts
- Screenshots on failure (and optionally on pass)
- Screen recordings (MP4)
- Step-by-step execution trace via `@Step`
- Environment info

---

## 🏗️ Design Patterns

| Pattern | Where Used |
|---------|-----------|
| **Page Object Model** | `pages/` — each screen = one class |
| **ThreadLocal Driver** | `DriverManager` — parallel-safe driver per thread |
| **Data-Driven Testing** | `DataProviderFactory` — Excel / JSON / Hashtable |
| **Listener Pattern** | `TestListener`, `AllureListener` — lifecycle hooks |
| **Utility Class** | `LogUtils`, `CaptureHelpers`, etc. — `final` + private constructor |
| **Factory Method** | `BaseTest` — `createAndroidDriver()`, `createIOSDriver()` |

---

## 🤝 Contributing

1. Branch from `main`
2. Follow existing code style (no `System.out.println`, use `LogUtils`)
3. Add `@Step` annotations to all Page Object methods
4. Run `mvn compile` to verify no compilation errors before opening PR
