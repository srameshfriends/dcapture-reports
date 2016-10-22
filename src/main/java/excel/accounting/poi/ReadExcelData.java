package excel.accounting.poi;

import excel.accounting.shared.FileHelper;
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
    private final String type;
    private final File file;
    private final ExcelTypeConverter<T> excelTypeConverter;

    public ReadExcelData(String type, File file, ExcelTypeConverter<T> excelTypeConverter) {
        this.type = type;
        this.file = file;
        this.excelTypeConverter = excelTypeConverter;
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
                T rowData = excelTypeConverter.getExcelType(type, getCellArray(columnCount, row));
                if (rowData != null) {
                    resultList.add(rowData);
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
                T rowData = excelTypeConverter.getExcelType(type, getCellArray(columnCount, row));
                if (rowData != null) {
                    resultList.add(rowData);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultList;
    }

    private Cell[] getCellArray(int columnCount, Row row) {
        Cell[] cellArray = new Cell[columnCount];
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cellArray[columnIndex] = cell;
        }
        return cellArray;
    }
}
