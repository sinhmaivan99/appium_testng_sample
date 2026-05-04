package dataproviders;

import constants.ConfigData;
import helpers.ExcelHelpers;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import java.util.Arrays;

/**
 * Centralized TestNG {@link DataProvider}s.
 * <p>
 * For Excel-backed providers, the source workbook is configured via
 * {@code EXCEL_DATA_FILE_PATH} in {@code config.properties}.
 * </p>
 */
public class DataProviderFactory {

    private static final String LOGIN_SHEET = "Login";
    private static final String DEFAULT_DYNAMIC_ROWS = "1,2,3";

    @DataProvider(name = "loginSuccess")
    public Object[][] loginSuccess() {
        return new Object[][]{
                {"admin", "admin123"},
                {"test", "test123"}
        };
    }

    @DataProvider(name = "loginFromExcel")
    public Object[][] loginFromExcel() {
        return new ExcelHelpers().getExcelData(ConfigData.EXCEL_DATA_FILE_PATH, LOGIN_SHEET);
    }

    @DataProvider(name = "loginFromExcelHashTable")
    public Object[][] loginFromExcelHashTable() {
        return new ExcelHelpers().getDataHashTable(
                ConfigData.EXCEL_DATA_FILE_PATH, LOGIN_SHEET, 2, 3);
    }

    @DataProvider(name = "loginSpecificRows")
    public Object[][] loginSpecificRows() {
        return new ExcelHelpers().getDataFromSpecificRows(
                ConfigData.EXCEL_DATA_FILE_PATH, LOGIN_SHEET, new int[]{1, 3});
    }

    @DataProvider(name = "loginSpecificRowsHashTable")
    public Object[][] loginSpecificRowsHashTable() {
        return new ExcelHelpers().getDataHashTableFromSpecificRows(
                ConfigData.EXCEL_DATA_FILE_PATH, LOGIN_SHEET, new int[]{1, 3});
    }

    /**
     * Reads rows specified by the {@code rowIndices} suite-XML parameter
     * (comma-separated, e.g. {@code "1,3,5"}). Defaults to {@code "1,2,3"}.
     */
    @DataProvider(name = "dynamicRows")
    public Object[][] dynamicRows(ITestContext context) {
        return new ExcelHelpers().getDataFromSpecificRows(
                ConfigData.EXCEL_DATA_FILE_PATH, LOGIN_SHEET, parseRowIndices(context));
    }

    /** Hashtable variant of {@link #dynamicRows(ITestContext)}. */
    @DataProvider(name = "dynamicRowsHashTable")
    public Object[][] dynamicRowsHashTable(ITestContext context) {
        return new ExcelHelpers().getDataHashTableFromSpecificRows(
                ConfigData.EXCEL_DATA_FILE_PATH, LOGIN_SHEET, parseRowIndices(context));
    }

    // ═══════════════════════ INTERNAL ═══════════════════════

    private static int[] parseRowIndices(ITestContext context) {
        String raw = context.getCurrentXmlTest().getParameter("rowIndices");
        if (raw == null || raw.isBlank()) {
            raw = DEFAULT_DYNAMIC_ROWS;
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}
