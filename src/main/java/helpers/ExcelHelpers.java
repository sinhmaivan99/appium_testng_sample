package helpers;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Helper class for reading and writing Apache POI Excel (.xlsx) files.
 * <p>
 * Supports both raw array format and {@link Hashtable} format for TestNG {@code @DataProvider}.
 * </p>
 */
public class ExcelHelpers {

    private Workbook workbook;
    private Sheet sheet;
    private Cell cell;
    private Row row;
    private String excelFilePath;
    private final Map<String, Integer> columns = new HashMap<>();

    // ═══════════════════════ SETUP ═══════════════════════

    /**
     * Opens the Excel file and loads the specified sheet.
     * Must be called before using row/cell accessors.
     *
     * @param excelPath absolute path to the Excel file
     * @param sheetName name of the sheet to open
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

    // ═══════════════════════ ROW ACCESS ═══════════════════════

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

    // ═══════════════════════ CELL READ ═══════════════════════

    public String getCellData(int rowNum, int colNum) {
        try {
            cell = sheet.getRow(rowNum).getCell(colNum);
            return extractCellValue(cell);
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

    // ═══════════════════════ CELL WRITE ═══════════════════════

    /**
     * Writes a value to a cell and applies colour coding (green for pass, red for fail).
     */
    public void setCellData(String text, int rowNumber, int colNumber) {
        writeCellData(text, rowNumber, colNumber);
    }

    public void setCellData(String text, int rowNumber, String columnName) {
        writeCellData(text, rowNumber, columns.get(columnName));
        LogUtils.info("Write to Excel: [row=" + rowNumber + ", col=" + columnName + "] = " + text);
    }

    // ═══════════════════════ DATA PROVIDERS ═══════════════════════

    /**
     * Returns all data rows (excluding header) as a 2D Object array.
     */
    public Object[][] getExcelData(String excelPath, String sheetName) {
        LogUtils.info("Reading all data from: " + excelPath + " | Sheet: " + sheetName);

        if (!new File(excelPath).exists()) {
            throw new IllegalArgumentException("Excel file not found: " + excelPath);
        }

        try (FileInputStream fis = new FileInputStream(excelPath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sh = wb.getSheet(sheetName);
            if (sh == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);

            Row headerRow = sh.getRow(0);
            int noOfRows = sh.getPhysicalNumberOfRows();
            int noOfCols = headerRow.getLastCellNum();
            LogUtils.info("Rows (excl. header): " + (noOfRows - 1) + " | Columns: " + noOfCols);

            Object[][] data = new Object[noOfRows - 1][noOfCols];
            for (int i = 1; i < noOfRows; i++) {
                for (int j = 0; j < noOfCols; j++) {
                    data[i - 1][j] = extractCellValue(sh.getRow(i).getCell(j));
                }
            }
            return data;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel data: " + excelPath, e);
        }
    }

    /**
     * Returns data rows from {@code startRow} to {@code endRow} (inclusive) as Hashtable array.
     */
    public Object[][] getDataHashTable(String excelPath, String sheetName, int startRow, int endRow) {
        LogUtils.info("Reading HashTable rows " + startRow + "-" + endRow
                + " from: " + excelPath + " | Sheet: " + sheetName);

        try (FileInputStream fis = new FileInputStream(excelPath)) {
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheet(sheetName);

            int cols = getColumns();
            Object[][] data = new Object[(endRow - startRow) + 1][1];

            for (int rowNum = startRow; rowNum <= endRow; rowNum++) {
                Hashtable<String, String> table = new Hashtable<>();
                for (int col = 0; col < cols; col++) {
                    table.put(getCellData(0, col), getCellData(rowNum, col));
                }
                data[rowNum - startRow][0] = table;
            }
            return data;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel data: " + excelPath, e);
        }
    }

    /**
     * Returns data from the specified row indices (1-based) as a 2D Object array.
     */
    public Object[][] getDataFromSpecificRows(String excelPath, String sheetName, int[] rowNumbers) {
        LogUtils.info("Reading rows " + Arrays.toString(rowNumbers)
                + " from: " + excelPath + " | Sheet: " + sheetName);

        try (FileInputStream fis = new FileInputStream(excelPath)) {
            workbook = WorkbookFactory.create(fis);
            sheet = workbook.getSheet(sheetName);

            if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);

            int cols = getColumns();
            Object[][] data = new Object[rowNumbers.length][cols];

            for (int i = 0; i < rowNumbers.length; i++) {
                int rowNum = rowNumbers[i];
                if (rowNum > sheet.getLastRowNum()) {
                    LogUtils.warn("Row " + rowNum + " does not exist — filling with empty strings");
                    for (int j = 0; j < cols; j++) data[i][j] = "";
                    continue;
                }
                for (int j = 0; j < cols; j++) {
                    data[i][j] = getCellData(rowNum, j);
                }
            }
            return data;

        } catch (Exception e) {
            throw new RuntimeException("Failed to read specific rows from Excel", e);
        }
    }

    /**
     * Returns data from the specified row indices as a Hashtable array.
     */
    public Object[][] getDataHashTableFromSpecificRows(String excelPath, String sheetName,
            int[] rowNumbers) {
        LogUtils.info("Reading HashTable rows " + Arrays.toString(rowNumbers)
                + " from: " + excelPath + " | Sheet: " + sheetName);

        try (FileInputStream fis = new FileInputStream(excelPath)) {
            workbook = WorkbookFactory.create(fis);
            sheet = workbook.getSheet(sheetName);

            if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);

            int cols = getColumns();
            Object[][] data = new Object[rowNumbers.length][1];

            for (int i = 0; i < rowNumbers.length; i++) {
                int rowNum = rowNumbers[i];
                if (rowNum > sheet.getLastRowNum()) {
                    LogUtils.warn("Row " + rowNum + " does not exist — using empty Hashtable");
                    data[i][0] = new Hashtable<String, String>();
                    continue;
                }
                Hashtable<String, String> table = new Hashtable<>();
                for (int j = 0; j < cols; j++) {
                    table.put(getCellData(0, j), getCellData(rowNum, j));
                }
                data[i][0] = table;
            }
            return data;

        } catch (Exception e) {
            throw new RuntimeException("Failed to read specific rows (HashTable) from Excel", e);
        }
    }

    // ═══════════════════════ PRIVATE HELPERS ═══════════════════════

    private String extractCellValue(Cell c) {
        if (c == null) return "";
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(c)
                    ? String.valueOf(c.getDateCellValue())
                    : String.valueOf((long) c.getNumericCellValue());
            case BOOLEAN -> Boolean.toString(c.getBooleanCellValue());
            case BLANK -> "";
            default -> "";
        };
    }

    private void writeCellData(String text, int rowNumber, int colNumber) {
        try {
            row = sheet.getRow(rowNumber);
            if (row == null) row = sheet.createRow(rowNumber);

            cell = row.getCell(colNumber);
            if (cell == null) cell = row.createCell(colNumber);

            cell.setCellValue(text);

            XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
            String lower = text.trim().toLowerCase();
            // Fix: use .equals() not == for String comparison
            if ("pass".equals(lower) || "passed".equals(lower) || "success".equals(lower)) {
                style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            } else if ("fail".equals(lower) || "failed".equals(lower) || "failure".equals(lower)) {
                style.setFillForegroundColor(IndexedColors.RED.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            cell.setCellStyle(style);

            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            LogUtils.error("Failed to write cell data [row=" + rowNumber
                    + ", col=" + colNumber + "]: " + e.getMessage(), e);
        }
    }
}