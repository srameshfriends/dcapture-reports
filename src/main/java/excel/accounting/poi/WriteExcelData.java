package excel.accounting.poi;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.model.StylesTable;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Write Excel Data
 */
public class WriteExcelData<T> {
    private String type;
    private final File file;
    private final ExcelTypeConverter<T> excelTypeConverter;
    private String dateFormatPattern;

    public WriteExcelData(String type, File file, ExcelTypeConverter<T> excelTypeConverter) {
        this.type = type;
        this.file = file;
        this.excelTypeConverter = excelTypeConverter;
        setDateFormat(new SimpleDateFormat("dd-MMM-yyyy"));
    }

    private void setDateFormat(SimpleDateFormat dateFormat) {
        if(dateFormat != null) {
            dateFormatPattern = DateFormatConverter.convert(Locale.UK, dateFormat.toPattern());
        }
    }

    private String getDateFormatPattern() {
        return dateFormatPattern;
    }

    public void writeRowData(List<T> dataList) {
        String[] columnNameArray = excelTypeConverter.getColumnNames();
        if (columnNameArray == null || 1 > columnNameArray.length) {
            throw new NullPointerException("Export to excel column names should not be empty");
        }
        try {
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("Excel Accounting");
            Row headerRow = sheet.createRow(0);
            addCell(workbook, headerRow, columnNameArray);
            int rowIndex = 1;
            for (T rowData : dataList) {
                Row row = sheet.createRow(rowIndex);
                Object[] cellData = excelTypeConverter.getExcelRow(type, rowData);
                if (cellData != null) {
                    addCell(workbook, row, cellData);
                }
                rowIndex += 1;
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addCell(Workbook workbook, Row row, Object[] objectArray) {
        int cellIndex = 0;
        for (Object obj : objectArray) {
            Cell cell = row.createCell(cellIndex);
            if (obj == null) {
                cell.setCellValue("");
            } else if (obj instanceof String) {
                cell.setCellValue((String) obj);
            } else if (obj instanceof Date) {
                cell.setCellValue((Date) obj);
                setCellDateFormat(workbook, cell);
            } else if (obj instanceof Double) {
                cell.setCellValue((Double) obj);
            } else if (obj instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) obj;
                cell.setCellValue(decimal.doubleValue());
            } else if (obj instanceof Integer) {
                cell.setCellValue((Integer) obj);
            }
            cellIndex += 1;
        }
    }

    private void setCellDateFormat(Workbook workbook, Cell cell) {
        CellStyle cellStyle = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        cellStyle.setDataFormat(dataFormat.getFormat(getDateFormatPattern()));
        cell.setCellStyle(cellStyle);
    }
}
