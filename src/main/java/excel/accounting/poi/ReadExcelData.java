package excel.accounting.poi;

import excel.accounting.shared.FileHelper;
import excel.accounting.shared.RowDataProvider;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Read Excel Data
 */
public class ReadExcelData<T> {
    private final File file;
    private final RowDataProvider<T> rowDataProvider;

    public ReadExcelData(File file, RowDataProvider<T> rowDataProvider) {
        this.file = file;
        this.rowDataProvider = rowDataProvider;
    }

    public List<T> readRowData(int columnCount, boolean ignoreHeader) {
        String extension = file == null ? null : FileHelper.getFileExtension(file);
        if (extension == null) {
            return new ArrayList<>();
        }
        extension = extension.toLowerCase();
        if ("xls".equals(extension)) {
            return readFromHSSF(columnCount, ignoreHeader);
        } else if ("xlsx".equals(extension)) {
            return readFromXSSF(columnCount, ignoreHeader);
        }
        return new ArrayList<>();
    }

    private List<T> readFromHSSF(int columnCount, boolean ignoreHeader) {
        List<T> resultList = new ArrayList<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
            HSSFSheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            for (int rowIndex = ignoreHeader ? 1 : 0; rowIndex < rowCount; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                List<Cell> cellList = getCellList(columnCount, row);
                if (!cellList.isEmpty()) {
                    T rowData = rowDataProvider.getRowData(rowIndex, cellList);
                    if (rowData != null) {
                        resultList.add(rowData);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultList;
    }

    private List<T> readFromXSSF(int columnCount, boolean ignoreHeader) {
        List<T> resultList = new ArrayList<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            for (int rowIndex = ignoreHeader ? 1 : 0; rowIndex < rowCount; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                List<Cell> cellList = getCellList(columnCount, row);
                if (!cellList.isEmpty()) {
                    T rowData = rowDataProvider.getRowData(rowIndex, cellList);
                    if (rowData != null) {
                        resultList.add(rowData);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultList;
    }

    private List<Cell> getCellList(int columnCount, Row row) {
        List<Cell> cellList = new ArrayList<>();
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cellList.add(cell);
        }
        return cellList;
    }
}
