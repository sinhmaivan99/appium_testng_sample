package helpers;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Helper for reading and writing Apache POI {@code .xlsx} files.
 * <p>
 * Two usage modes:
 * <ul>
 *     <li>Stateful: call {@link #setExcelFile(String, String)} once, then use the
 *         row/cell accessors and {@link #setCellData(String, int, int)}.</li>
 *     <li>Stateless: call the {@code getExcelData / getDataHashTable / getDataFromSpecificRows}
 *         overloads directly — they open and close the workbook each call.</li>
 * </ul>
 */
public class ExcelHelpers {

    private static final Set<String> PASS_KEYWORDS = Set.of("pass", "passed", "success");
    private static final Set<String> FAIL_KEYWORDS = Set.of("fail", "failed", "failure");

    private Workbook workbook;
    private Sheet sheet;
    private String excelFilePath;
    private final Map<String, Integer> columns = new HashMap<>();

    // ═══════════════════════ SETUP ═══════════════════════

    /**
     * Opens the Excel file and loads the specified sheet for stateful operations.
     */
    public void setExcelFile(String excelPath, String sheetName) {
        LogUtils.info("Opening Excel file: " + excelPath + " | Sheet: " + sheetName);
        if (!new File(excelPath).exists()) {
            throw new IllegalArgumentException("Excel file not found: " + excelPath);
        }
        if (sheetName == null || sheetName.isBlank()) {
            throw new IllegalArgumentException("Sheet name must not be blank");
        }

        try (FileInputStream fis = new FileInputStream(excelPath)) {
            workbook = WorkbookFactory.create(fis);
            sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }
            excelFilePath = excelPath;
            columns.clear();
            sheet.getRow(0).forEach(c -> columns.put(c.getStringCellValue(), c.getColumnIndex()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open Excel file: " + excelPath, e);
        }
    }

    // ═══════════════════════ ROW / COL ACCESS (stateful) ═══════════════════════

    public Row getRowData(int rowNum) {
        return sheet.getRow(rowNum);
    }

    public int getRows() {
        return sheet.getLastRowNum();
    }

    public int getColumns() {
        return sheet.getRow(0).getLastCellNum();
    }

    public int getRowContains(String testCaseName, int colNum) {
        int rowCount = getRows();
        for (int i = 0; i < rowCount; i++) {
            if (getCellData(i, colNum).equalsIgnoreCase(testCaseName)) {
                return i;
            }
        }
        return -1;
    }

    public String getCellData(int rowNum, int colNum) {
        try {
            return extractCellValue(sheet.getRow(rowNum).getCell(colNum));
        } catch (Exception e) {
            return "";
        }
    }

    public String getCellData(int rowNum, String columnName) {
        return getCellData(rowNum, columns.get(columnName));
    }

    public String getCellData(String columnName, int rowNum) {
        return getCellData(rowNum, columns.get(columnName));
    }

    // ═══════════════════════ CELL WRITE (stateful) ═══════════════════════

    public void setCellData(String text, int rowNumber, int colNumber) {
        writeCellData(text, rowNumber, colNumber);
    }

    public void setCellData(String text, int rowNumber, String columnName) {
        writeCellData(text, rowNumber, columns.get(columnName));
        LogUtils.info("Wrote to Excel: [row=" + rowNumber + ", col=" + columnName + "] = " + text);
    }

    // ═══════════════════════ DATA PROVIDERS (stateless) ═══════════════════════

    /** Returns all data rows (excluding header) as a 2D Object array. */
    public Object[][] getExcelData(String excelPath, String sheetName) {
        return readSheet(excelPath, sheetName, sh -> {
            Row header = sh.getRow(0);
            int totalRows = sh.getPhysicalNumberOfRows();
            int totalCols = header.getLastCellNum();
            LogUtils.info("Rows (excl. header): " + (totalRows - 1) + " | Columns: " + totalCols);

            Object[][] data = new Object[totalRows - 1][totalCols];
            for (int i = 1; i < totalRows; i++) {
                for (int j = 0; j < totalCols; j++) {
                    data[i - 1][j] = extractCellValue(sh.getRow(i).getCell(j));
                }
            }
            return data;
        });
    }

    /** Returns rows {@code [startRow, endRow]} (inclusive) as a Hashtable array. */
    public Object[][] getDataHashTable(String excelPath, String sheetName, int startRow, int endRow) {
        LogUtils.info("Reading Hashtable rows " + startRow + "-" + endRow);
        return readSheet(excelPath, sheetName, sh -> {
            int cols = sh.getRow(0).getLastCellNum();
            Object[][] data = new Object[(endRow - startRow) + 1][1];
            for (int rowNum = startRow; rowNum <= endRow; rowNum++) {
                data[rowNum - startRow][0] = buildRowHashtable(sh, rowNum, cols);
            }
            return data;
        });
    }

    /** Returns the specified rows (1-based) as a 2D Object array. */
    public Object[][] getDataFromSpecificRows(String excelPath, String sheetName, int[] rowNumbers) {
        LogUtils.info("Reading rows " + Arrays.toString(rowNumbers));
        return readSheet(excelPath, sheetName, sh -> {
            int cols = sh.getRow(0).getLastCellNum();
            Object[][] data = new Object[rowNumbers.length][cols];
            for (int i = 0; i < rowNumbers.length; i++) {
                int rowNum = rowNumbers[i];
                if (rowNum > sh.getLastRowNum()) {
                    LogUtils.warn("Row " + rowNum + " does not exist — filling with empty strings");
                    Arrays.fill(data[i], "");
                    continue;
                }
                for (int j = 0; j < cols; j++) {
                    data[i][j] = extractCellValue(sh.getRow(rowNum).getCell(j));
                }
            }
            return data;
        });
    }

    /** Hashtable variant of {@link #getDataFromSpecificRows(String, String, int[])}. */
    public Object[][] getDataHashTableFromSpecificRows(String excelPath, String sheetName,
                                                      int[] rowNumbers) {
        LogUtils.info("Reading Hashtable rows " + Arrays.toString(rowNumbers));
        return readSheet(excelPath, sheetName, sh -> {
            int cols = sh.getRow(0).getLastCellNum();
            Object[][] data = new Object[rowNumbers.length][1];
            for (int i = 0; i < rowNumbers.length; i++) {
                int rowNum = rowNumbers[i];
                if (rowNum > sh.getLastRowNum()) {
                    LogUtils.warn("Row " + rowNum + " does not exist — using empty Hashtable");
                    data[i][0] = new Hashtable<String, String>();
                    continue;
                }
                data[i][0] = buildRowHashtable(sh, rowNum, cols);
            }
            return data;
        });
    }

    // ═══════════════════════ INTERNAL ═══════════════════════

    @FunctionalInterface
    private interface SheetReader<T> {
        T read(Sheet sheet);
    }

    private <T> T readSheet(String excelPath, String sheetName, SheetReader<T> reader) {
        if (!new File(excelPath).exists()) {
            throw new IllegalArgumentException("Excel file not found: " + excelPath);
        }
        try (FileInputStream fis = new FileInputStream(excelPath);
             Workbook wb = WorkbookFactory.create(fis)) {
            Sheet sh = wb.getSheet(sheetName);
            if (sh == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }
            return reader.read(sh);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel: " + excelPath, e);
        }
    }

    private static Hashtable<String, String> buildRowHashtable(Sheet sh, int rowNum, int cols) {
        Hashtable<String, String> table = new Hashtable<>();
        Row headerRow = sh.getRow(0);
        Row dataRow = sh.getRow(rowNum);
        for (int j = 0; j < cols; j++) {
            String header = extractCellValue(headerRow.getCell(j));
            String value = extractCellValue(dataRow == null ? null : dataRow.getCell(j));
            table.put(header, value);
        }
        return table;
    }

    private static String extractCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? String.valueOf(cell.getDateCellValue())
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private void writeCellData(String text, int rowNumber, int colNumber) {
        try {
            Row targetRow = sheet.getRow(rowNumber);
            if (targetRow == null) targetRow = sheet.createRow(rowNumber);

            Cell targetCell = targetRow.getCell(colNumber);
            if (targetCell == null) targetCell = targetRow.createCell(colNumber);

            targetCell.setCellValue(text);
            targetCell.setCellStyle(buildResultStyle(text));

            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            LogUtils.error("Failed to write cell [row=" + rowNumber + ", col=" + colNumber + "]: "
                    + e.getMessage(), e);
        }
    }

    private XSSFCellStyle buildResultStyle(String text) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        String lower = text == null ? "" : text.trim().toLowerCase();
        if (PASS_KEYWORDS.contains(lower)) {
            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } else if (FAIL_KEYWORDS.contains(lower)) {
            style.setFillForegroundColor(IndexedColors.RED.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
